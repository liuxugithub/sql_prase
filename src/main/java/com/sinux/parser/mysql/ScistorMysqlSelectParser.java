package com.sinux.parser.mysql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.sinux.parser.column.ScistorColumn;
import com.sinux.parser.column.ScistorJdbcColumn;
import com.sinux.parser.column.ScistorSelectColumn;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.result.ScistorSQLType;
import com.sinux.parser.result.ScistorSelectResult;
import com.sinux.parser.table.ScistorTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ���ڲ��select��,�滻���� select�к�where��
 * @author GuoLiang
 */
public class ScistorMysqlSelectParser extends ScistorMysqlConditionParser{
	
	public ScistorMysqlSelectParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorSelectResult();
		this.result.setSqlType(ScistorSQLType.SELECT);
	}
	public static final int ONLY_WHERE = 1;
	public static final int ONLY_SELECT = 2;
	public static final int NO_STAR = 3;
	
	private int starNumbers = 0;
	private List<ScistorSelectColumn> selectColumns;
	private SQLSelectQuery selectQuery;

	public void addCondition(String left, String right, SQLBinaryOperator operator){
		SQLBinaryOpExpr whereOpExpr = new SQLBinaryOpExpr();
		whereOpExpr.setParent(selectQuery);
		whereOpExpr.setOperator(operator);
		SQLIdentifierExpr leftExpr = new SQLIdentifierExpr();
		SQLIdentifierExpr rightExpr = new SQLIdentifierExpr();
		leftExpr.setName(left);
		leftExpr.setParent(whereOpExpr);
		rightExpr.setName(right);
		rightExpr.setParent(whereOpExpr);
		whereOpExpr.setLeft(leftExpr);
		whereOpExpr.setRight(rightExpr);
		((SQLSelectStatement) this.statement).addWhere(whereOpExpr);
		this.result.setStatement(this.statement);
	}
	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		if(this.statement instanceof SQLSelectStatement){
			SQLSelectStatement st = (SQLSelectStatement) this.statement;
			SQLSelectQuery query = st.getSelect().getQuery();
			if(query instanceof MySqlSelectQueryBlock){
				this.selectQuery=query;

				parse(query);
				
				
			}//else if(query instanceof MySqlUnionQuery){
			 else if(query instanceof SQLUnionQuery){

				
				List<SQLSelectQuery> queryLists = new ArrayList<SQLSelectQuery>();
				//MySqlUnionQuery union = (MySqlUnionQuery) query;
				SQLUnionQuery union = (SQLUnionQuery) query;
				getQueryListFromUnion(union.getLeft(), queryLists);
				getQueryListFromUnion(union.getRight(), queryLists);
				
				//modify by cx 20171217 ��������union�Ӿ�
				for(int i =0;i<queryLists.size();i++){
					parse(queryLists.get(i));
				}
				

			}
		}else{
			throw new ScistorParserException("SQL Error:Not a select sql!");
		}
		return this.result;
	}

	private void parse(SQLSelectQuery query) throws ScistorParserException{
		MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) query;
		List<ScistorSelectColumn> selectedColumns = new ArrayList<ScistorSelectColumn>();
		List<SQLSelectItem> items = queryBlock.getSelectList();
		parseSelectItems(items, selectedColumns);
		
		for(ScistorSelectColumn column : selectedColumns){
			if(column.isSelectAll()&&!column.isAggregator()){
				((ScistorSelectResult)this.result).setSelectAll(true);
				starNumbers++;
			}
		}
		if(!((ScistorSelectResult)this.result).isSelectAll()){
			parseQuery(query,selectedColumns,NO_STAR);
		}else{
			this.selectColumns = selectedColumns;
			this.selectQuery = query;
			parseQuery(query,new ArrayList<ScistorSelectColumn>(),ONLY_WHERE);
		}
	}
	/**
	 * ��select�к���  * ��ʱ��JDBC��ȡ��ResultSetMetaData������ ������  ���ô˷�����
	 * Ҳ����ÿ��ִ��Select��ѯ���ô˷���
	 * @param columnNames
	 * @throws ScistorParserException
	 */
	public void setStarColumns(List<ScistorJdbcColumn> columnNames) throws ScistorParserException{
		if(this.selectColumns==null){
			return;
		}
		if(this.selectColumns.size()>0){
			String owner = null;
			if(this.selectColumns.size()==1){
				owner = this.selectColumns.get(0).getOwner();
			}
			this.selectColumns.clear();
			for(ScistorJdbcColumn columnname : columnNames){
				if(columnname.getOwner()!=null&&!columnname.getOwner().equals("*")){
					ScistorSelectColumn column = new ScistorSelectColumn();
					column.setName(columnname.getName());
					column.setOwner(columnname.getOwner());
					column.setFromJdbc(true);
					if(columnname.getOwner()!=null){
						column.setOrginTable(true);
					}
					this.selectColumns.add(column);
				}else{
//					if(starNumbers>1) throw new ScistorParserException("SQL Error : not support more than two '*' for database ?" );
					this.selectColumns.add(new ScistorSelectColumn(owner,columnname.getName()));
				}
			}
			SQLSelectQuery query = this.selectQuery;
			parseQuery(query, this.selectColumns,ONLY_SELECT);
		}else{
			List<ScistorSelectColumn> selectedColumns = ((ScistorSelectResult) this.result).getSelectColumns();
			if(selectedColumns==null) throw new ScistorParserException("Parse Error!");
			int size = selectedColumns.size();
			if(size == columnNames.size()){
				for(int i = 0;i<size;i++){
					ScistorSelectColumn column = selectedColumns.get(i);
					if(column.getOwner()==null){
						column.setOwner(columnNames.get(i).getOwner());
					}
				}
			}
		}
	}
	
	/**
	 * ��� ������select�е�result
	 * @param query
	 * @param selectedColumns
	 * @param type
	 * @throws ScistorParserException
	 */
	private void parseQuery(SQLSelectQuery query,List<ScistorSelectColumn> selectColumns,int part) throws ScistorParserException {
		if(query instanceof MySqlSelectQueryBlock){
			MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) query;
			if(queryBlock.getInto()!=null) throw new ScistorParserException("SQL ERROR : select .. into.. syntax is not supported");
			List<ScistorSelectColumn> selectedColumns = selectColumns;
			if(result.haveWhere()==null){
				result.setHaveWhere(queryBlock.getWhere()!=null);
				List<ScistorColumn> columns = new ArrayList<>();
				if(queryBlock.getWhere()!=null){
					findWhereColumns((SQLBinaryOpExpr)queryBlock.getWhere(),columns);
					result.addAllWhereColumns(columns);
				}else {
					result.setHaveWhere(false);
				}
			}
			if(queryBlock.getGroupBy()!=null){
				SQLSelectGroupByClause groupByClause =  queryBlock.getGroupBy();
				StringBuilder content = new StringBuilder("group by  ");
				groupByClause.getItems().forEach(expr->{
					content.append(expr.toString()).append(",");
				});
				content.deleteCharAt(content.length()-1);

				if(groupByClause.getHaving()!=null){
					content.append(" having ").append(groupByClause.getHaving().toString());
				}
				this.result.setGroupStr(content.toString());
			}
			if(queryBlock.getLimit()!=null){
				SQLLimit limit  = queryBlock.getLimit();
				String limitStr = "limit ";
				if(limit.getOffset()!=null){
					SQLIntegerExpr offsetExpr = (SQLIntegerExpr) limit.getOffset();
					limitStr+=offsetExpr.getNumber()+",";
				}
				if(limit.getRowCount()!=null){
					SQLIntegerExpr rowCount = (SQLIntegerExpr) limit.getRowCount();
					limitStr+=rowCount.getNumber()+" ";
				}
				this.result.setLimitStr(limitStr);
			}
			analyzeAndParse(queryBlock, selectedColumns, null, true ,part);
		}else{
			throw new ScistorParserException("Parse Error : can identified class "+query.getClass());
		}
	}

	private void findWhereColumns(SQLBinaryOpExpr whereOpExpr,List<ScistorColumn> columns){
		SQLExpr leftExpr = whereOpExpr.getLeft();
		SQLExpr rightExpr = whereOpExpr.getRight();
		if(leftExpr!=null && leftExpr instanceof SQLPropertyExpr){
			SQLPropertyExpr leftSqlProp = (SQLPropertyExpr) leftExpr;
			ScistorSelectColumn selectColumn = new ScistorSelectColumn();
			selectColumn.setName(leftSqlProp.getName());
			if(leftSqlProp.getOwner() instanceof SQLIdentifierExpr){
				selectColumn.setOwner(((SQLIdentifierExpr)leftSqlProp.getOwner()).getName());
			}
			columns.add(selectColumn);
		}else if(leftExpr!=null &&  leftExpr instanceof  SQLBinaryOpExpr){
			findWhereColumns((SQLBinaryOpExpr)leftExpr,columns);
		}
		if(rightExpr!=null &&  rightExpr instanceof SQLPropertyExpr){
			SQLPropertyExpr rightSqlProp = (SQLPropertyExpr) rightExpr;
			ScistorSelectColumn selectColumn = new ScistorSelectColumn();
			selectColumn.setName(rightSqlProp.getName());
			if(rightSqlProp.getOwner() instanceof SQLIdentifierExpr){
				selectColumn.setOwner(((SQLIdentifierExpr)rightSqlProp.getOwner()).getName());
			}
			columns.add(selectColumn);
		}else if(rightExpr!=null && rightExpr instanceof  SQLBinaryOpExpr){
			findWhereColumns((SQLBinaryOpExpr)rightExpr,columns);
		}
	}

	/**
	 * ��Ƕ�׵����滻������
	 * @param subQuery
	 * @param thisQueryAlias
	 * @param type
	 * @throws ScistorParserException
	 */
	private void replaceWithSubQuery(SQLSelectQuery subQuery,String thisQueryAlias,int part) throws ScistorParserException{
		if(subQuery instanceof MySqlSelectQueryBlock){
			
			MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) subQuery;
			if(queryBlock.getInto()!=null) throw new ScistorParserException("SQL ERROR : select .. into.. syntax is not supported");
			List<ScistorSelectColumn> inselectedColumns = getSelectColumns(queryBlock);
			analyzeAndParse(queryBlock, inselectedColumns, thisQueryAlias, false ,part);
			
		}else{
			throw new ScistorParserException("Parse Error : can identified class "+subQuery.getClass());
		}
	}

	private void analyzeAndParse(MySqlSelectQueryBlock queryBlock,List<ScistorSelectColumn> selectedColumns,
			String thisQueryAlias,boolean OUTEST,int part) throws ScistorParserException{
		List<ScistorColumn> whereColumns = new ArrayList<ScistorColumn>();
		List<SQLSelectQuery> subWhereQuerys = new ArrayList<SQLSelectQuery>();
		
		if(!OUTEST){
			if(part != ONLY_SELECT){
				whereColumns = getAllConditionColumnExceptJoinOn(queryBlock, subWhereQuerys);
				replaceWhereCNameWithSelectCAliasName(whereColumns, selectedColumns);
			}
		}else{
			if(part != ONLY_SELECT){
				whereColumns = getAllConditionColumnExceptJoinOn(queryBlock, subWhereQuerys);
				if(part == NO_STAR){
					replaceWhereCNameWithSelectCAliasName(whereColumns, selectedColumns);
				}
			}
		}
		
		if(thisQueryAlias!=null){
			for(ScistorSelectColumn column : selectedColumns){
				if(column.isSelectAll()) throw new ScistorParserException("SQL ERROR : not supported '*' in subquery or union clause");
				column.setSubQueryAlias(thisQueryAlias);
			}
			for(ScistorColumn column : whereColumns){
				column.setSubQueryAlias(thisQueryAlias);
			}
		}
		
		SQLTableSource tableSource = queryBlock.getFrom();
		
		parseTableSource(tableSource, selectedColumns, whereColumns, OUTEST,part);

		if(part != ONLY_SELECT){
			if(subWhereQuerys.size()>0){
				for(SQLSelectQuery subquery : subWhereQuerys){
					replaceWithSubQuery(subquery,sefDefindSubAlias+(++selfDefinedSubAliasID),NO_STAR);
				}
			}
		}
	}
	
	private void parseTableSource(SQLTableSource tableSource,List<ScistorSelectColumn> selectedColumns,
			List<ScistorColumn> whereColumns,boolean OUTEST,int part) throws ScistorParserException{
		if(tableSource instanceof SQLExprTableSource){
			SQLExprTableSource table = (SQLExprTableSource) tableSource;
			parseExprTableSource(table, selectedColumns, whereColumns,OUTEST, part);
		}else if(tableSource instanceof SQLJoinTableSource){
			parseJoinTableSource(tableSource, selectedColumns, whereColumns, OUTEST, part);
		}else if(tableSource instanceof SQLSubqueryTableSource){
			SQLSubqueryTableSource subsubQuery = (SQLSubqueryTableSource) tableSource;
			parseSubQueryTableSource(subsubQuery, selectedColumns, whereColumns, OUTEST, part);
		}else if(tableSource instanceof SQLUnionQueryTableSource){
			SQLUnionQueryTableSource subUnion = (SQLUnionQueryTableSource) tableSource;
			parseSubUnionTableSource(subUnion, selectedColumns, whereColumns, OUTEST, part);
		}else {
			throw new ScistorParserException("'"+tableSource.getClass()+"' is not supported");
		}
	}
	private void parseExprTableSource(SQLExprTableSource tablesource,List<ScistorSelectColumn> selectedColumns,
			List<ScistorColumn> whereColumns,boolean OUTEST, int part) throws ScistorParserException{
		SQLExpr tableExpr = tablesource.getExpr();
		if(!(tableExpr instanceof SQLIdentifierExpr)&&!(tableExpr instanceof SQLPropertyExpr)){
			throw new ScistorParserException("SQL ERROR : Not Supported Format : "+tableExpr.toString());
		}
		//!(tableExpr instanceof SQLIdentifierExpr)&&!(tableExpr instanceof SQLPropertyExpr);
		String tablename ="";
		if(tableExpr instanceof SQLIdentifierExpr){
			 tablename = ((SQLIdentifierExpr)tableExpr).getName();
		}else if(tableExpr instanceof SQLPropertyExpr ){
			tablename = ((SQLPropertyExpr)tableExpr).getName();
		}
		if(OUTEST){
			if(part != ONLY_WHERE){
				for(ScistorSelectColumn column : selectedColumns){
					column.setOwner(tablename);
					((ScistorSelectResult)this.result).addSelectColumn(column);
				}
			}
		}else{
			for(ScistorSelectColumn column : selectedColumns){
				column.setOwner(tablename);
				((ScistorSelectResult)this.result).replace(column,part);
				if(part != ONLY_SELECT && !column.isSelectAll()){
					((ScistorSelectResult)this.result).addConditionColumn(column);
				}
			}
		}
		if(part != ONLY_SELECT){
			for(ScistorColumn column : whereColumns){
				column.setOwner(tablename);
				((ScistorSelectResult)this.result).addConditionColumn(column);
			} 
		}
	}
	
	private void parseJoinTableSource(SQLTableSource tableSource,List<ScistorSelectColumn> selectedColumns,
			List<ScistorColumn> whereColumns,boolean OUTEST, int part) throws ScistorParserException{
		
		Map<String,SQLSelectQuery> subQuerys = new HashMap<String,SQLSelectQuery>();
		List<ScistorTable> tables = new ArrayList<ScistorTable>();
		List<ScistorColumn> onColumns = new ArrayList<ScistorColumn>();
		parseJoinTableSource(tableSource,subQuerys,tables,onColumns);
		this.result.addTables(tables);
		for(ScistorColumn column : onColumns){
			column.setIsWhere(true);
		}
		
		replaceWhereCNameWithSelectCAliasName(onColumns, selectedColumns);

		whereColumns.addAll(onColumns);
		
		if(subQuerys.size()==0){ // there is no sub query
			if(OUTEST){
				if(part != ONLY_WHERE){
					for(ScistorSelectColumn column : selectedColumns){
						findColumnTable(column, null,tables);
						((ScistorSelectResult)this.result).addSelectColumn(column);
					}
				}
			}else{
				for(ScistorSelectColumn column : selectedColumns){
					findColumnTable(column, null,tables);
					((ScistorSelectResult)this.result).replace(column,part);
					if(part != ONLY_SELECT && !column.isSelectAll()){
						((ScistorSelectResult)this.result).addConditionColumn(column);
					}
				}
			}
			if(part != ONLY_SELECT){
				for(ScistorColumn column : whereColumns){
					findColumnTable(column, null, tables);
					((ScistorSelectResult)this.result).addConditionColumn(column);
				}
			}
		}else if(tables.size()==0){ // only sub querys
			if(OUTEST){
				if(part != ONLY_WHERE){
					if(this.starNumbers>0){
						asignSubAliasForDuplicate(selectedColumns, subQuerys);
					}
					for(ScistorSelectColumn column : selectedColumns){
						((ScistorSelectResult)this.result).addSelectColumn(column);
					}
				}
			}else{
				for(ScistorSelectColumn column : selectedColumns){
					((ScistorSelectResult)this.result).replace(column,part);
					if(part != ONLY_SELECT && !column.isSelectAll()){
						this.result.addConditionColumn(column);
					}
				}
			}
			if(part != ONLY_SELECT){
				for(ScistorColumn column : whereColumns){
					this.result.addConditionColumn(column);
				}
			}
			for(String subAlias : subQuerys.keySet()){
				replaceWithSubQuery(subQuerys.get(subAlias), subAlias,part);
			}
		}else{ // there is tablename and subquery
			Map<String,List<ScistorSelectColumn>> subSelectColumns = new HashMap<String,List<ScistorSelectColumn>>();
			for(String subAlias : subQuerys.keySet()){
				subSelectColumns.put(subAlias, getSubQuerySelectColumns(subAlias,subQuerys.get(subAlias)));
			}
			if(OUTEST){
				if(part != ONLY_WHERE){
					for(ScistorSelectColumn column : selectedColumns){
						findColumnTable(column, subSelectColumns, tables);
						((ScistorSelectResult)this.result).addSelectColumn(column);
					}
				}
			}else{
				for(ScistorSelectColumn column : selectedColumns){
					findColumnTable(column, subSelectColumns, tables);
					((ScistorSelectResult)this.result).replace(column,part);
					if(part != ONLY_SELECT && !column.isSelectAll()){
						((ScistorSelectResult)this.result).addConditionColumn(column);
					}
				}
			}
			if(part != ONLY_SELECT){
				for(ScistorColumn column : whereColumns){
					findColumnTable(column, subSelectColumns, tables);
					this.result.addConditionColumn(column);
				}
			}
			
			for(String subAlias : subQuerys.keySet()){
				replaceWithSubQuery(subQuerys.get(subAlias), subAlias,part);
			}
		}
	}
	
	private void parseSubQueryTableSource(SQLSubqueryTableSource tableSource,List<ScistorSelectColumn> selectedColumns,
			List<ScistorColumn> whereColumns,boolean OUTEST, int part) throws ScistorParserException{
		String subAlias = tableSource.getAlias();
		Map<String,List<ScistorSelectColumn>> subSelectColumns = new HashMap<String,List<ScistorSelectColumn>>();
		subSelectColumns.put(subAlias, getSubQuerySelectColumns(subAlias,tableSource.getSelect().getQuery()));
		if(OUTEST){
			if(part != ONLY_WHERE){
				for(ScistorSelectColumn column : selectedColumns){
					checkUnknowColumn(column, subSelectColumns);
					((ScistorSelectResult)this.result).addSelectColumn(column);
				}
			}
		}else{
			for(ScistorSelectColumn column : selectedColumns){
				checkUnknowColumn(column, subSelectColumns);
				((ScistorSelectResult)this.result).replace(column,part);
				if(part != ONLY_SELECT && !column.isSelectAll()){
					((ScistorSelectResult)this.result).addConditionColumn(column);
				}
			}
		}
		if(part != ONLY_SELECT){
			for(ScistorColumn column : whereColumns){
				checkUnknowColumn(column, subSelectColumns);
				this.result.addConditionColumn(column);
			}
		}
			
		replaceWithSubQuery(tableSource.getSelect().getQuery(), subAlias,part);
	}
	
	private void parseSubUnionTableSource(SQLUnionQueryTableSource tableSource,List<ScistorSelectColumn> selectedColumns,
			List<ScistorColumn> whereColumns,boolean OUTEST, int part) throws ScistorParserException{
		String subAlias = tableSource.getAlias();
		/*
		 * Ŀǰֻ������������union,�Ҳ��������ʱ��û�д���
		 */
		List<SQLSelectQuery> queryLists = new ArrayList<SQLSelectQuery>();
		//MySqlUnionQuery union = (MySqlUnionQuery) tableSource.getUnion();
		SQLUnionQuery union = (SQLUnionQuery) tableSource.getUnion();
		getQueryListFromUnion(union.getLeft(), queryLists);
		getQueryListFromUnion(union.getRight(), queryLists);
		
		Map<String,List<ScistorSelectColumn>> subSelectColumns = new HashMap<String,List<ScistorSelectColumn>>();
		subSelectColumns.put(subAlias, getSubQuerySelectColumns(subAlias,queryLists.get(0)));
		
		if(OUTEST){
			if(part != ONLY_WHERE){
				for(ScistorSelectColumn column : selectedColumns){
					checkUnknowColumn(column, subSelectColumns);
					((ScistorSelectResult)this.result).addSelectColumn(column);
				}
			}
		}else{
			for(ScistorSelectColumn column : selectedColumns){
				checkUnknowColumn(column, subSelectColumns);
				((ScistorSelectResult)this.result).replace(column,part);
				if(part != ONLY_SELECT && !column.isSelectAll()){
					((ScistorSelectResult)this.result).addConditionColumn(column);
				}
			}
		}
		if(part != ONLY_SELECT){
			for(ScistorColumn column : whereColumns){
				checkUnknowColumn(column, subSelectColumns);
				this.result.addConditionColumn(column);
			}
		}
		for(int i=0;i<queryLists.size();i++){
			replaceWithSubQuery(queryLists.get(i), subAlias, part);
		}
	}

	/**
	 * ��Ƕ�ײ�ѯʱ������������Ƿ��������Ӳ�ѯ��
	 * @param column
	 * @param subSelectColumns
	 * @throws ScistorParserException
	 */
	private void checkUnknowColumn(ScistorColumn column,Map<String, List<ScistorSelectColumn>> subSelectColumns) throws ScistorParserException{
		if(!inSubSelect(column, subSelectColumns)){
			throw new ScistorParserException("SQL ERROR : Unknown column : "+ column.getName());
		}
	}
	
	private void asignSubAliasForDuplicate(List<ScistorSelectColumn> selectedColumns,Map<String,SQLSelectQuery> subQuerys) throws ScistorParserException{
		int size = selectedColumns.size();
		Map<String,List<ScistorSelectColumn>> subSelectColumns = new HashMap<String,List<ScistorSelectColumn>>();
		for(String subAlias : subQuerys.keySet()){
			subSelectColumns.put(subAlias, getSubQuerySelectColumns(subAlias,subQuerys.get(subAlias)));
		}
		for(int i=0;i<size-1;i++){
			int k = -1;
			ScistorSelectColumn column = selectedColumns.get(i);
			if(column.getOwner()!=null) continue;
			List<String> aliasNames = new ArrayList<String>();
			for(int j=i+1;j<size;j++){
				ScistorSelectColumn column1 = selectedColumns.get(j);
				if(column1.getOwner()!=null) continue;
				if(column.getName().equals(column1.getName())){
					if(aliasNames.isEmpty()){
						getInSubSelectAliases(column, subSelectColumns,aliasNames);
					}
					if(column.getSubQueryAlias()==null){
						++k;
						if(k>aliasNames.size()) throw new ScistorParserException("SQL ERROR:"+"Unknow column "+column.getName());
						column.setOwner(aliasNames.get(k));
					}
					
					if(column1.getSubQueryAlias()==null){
						++k;
						if(k>aliasNames.size()) throw new ScistorParserException("SQL ERROR:"+"Unknow column "+column1.getName());
						column1.setOwner(aliasNames.get(k));
					}
				}
			}
		}
	}
}
