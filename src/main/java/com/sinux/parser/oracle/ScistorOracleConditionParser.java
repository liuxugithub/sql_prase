package com.sinux.parser.oracle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
//import com.alibaba.druid.sql.dialect.Oracle.ast.expr.OracleSelectGroupByExpr;
//import com.alibaba.druid.sql.dialect.Oracle.ast.statement.OracleUnionQuery;
import com.sinux.parser.column.ScistorColumn;
import com.sinux.parser.column.ScistorSelectColumn;
import com.sinux.parser.column.ScistorTextColumn;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorColumnResult;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.table.ScistorTable;

public abstract class ScistorOracleConditionParser  extends ScistorOracleParser{
	protected int selfDefinedSubAliasID = 0;
	protected static final String sefDefindSubAlias = "Scistor";
	protected ScistorColumnResult result;
	public ScistorOracleConditionParser(SQLStatement statement) {
		super(statement);
	}

	@Override
	public  abstract ScistorResult getParseResult() throws ScistorParserException;
	
	/*
	 * ��ȡselect�� 
	 */
	protected List<ScistorSelectColumn> getSelectColumns(OracleSelectQueryBlock queryBlock) throws ScistorParserException{
		List<ScistorSelectColumn> inselectedColumns = new ArrayList<ScistorSelectColumn>();
		List<SQLSelectItem> items = queryBlock.getSelectList();
		parseSelectItems(items, inselectedColumns);
		return inselectedColumns;
	}
	
	/*
	 * ��ȡ���� ����join on ����� �����ֶ�
	 */
	protected List<ScistorColumn> getAllConditionColumnExceptJoinOn(OracleSelectQueryBlock queryBlock,
			List<SQLSelectQuery> subQuerys) throws ScistorParserException{
		List<ScistorColumn> inwhereColumns = new ArrayList<ScistorColumn>();
		SQLExpr where = queryBlock.getWhere();
		parseWhere(where,inwhereColumns, subQuerys);
		
		SQLSelectGroupByClause groupby = queryBlock.getGroupBy();
		parseGroupBy(groupby, inwhereColumns);
		
		SQLOrderBy orderby = queryBlock.getOrderBy();
		parseOrderBy(orderby, inwhereColumns);
		
		for(ScistorColumn column : inwhereColumns){
			column.setIsWhere(true);
		}
		
		return inwhereColumns;
	}
	
	/*
	 * group by ��  order by �п���ʹ��select �е��� ��������select��ʵ�������滻 ����
	 * ��select�е��ֶ� �滻 where�еı��� 
	 */
	protected void replaceWhereCNameWithSelectCAliasName(List<ScistorColumn> whereColumns,
				List<ScistorSelectColumn> selectedColumns) throws ScistorParserException{
		for(ScistorColumn column : whereColumns){
			for(ScistorSelectColumn scolumn : selectedColumns){
				if(scolumn.getAlias()!=null){
					if(scolumn.getAlias().equals(column.getName())){
						column.setName(scolumn.getName());
					}
				}
			}
		}
	}
	
	/*
	 * ���� select ��Ԫ��
	 */
	protected void parseSelectItems(List<SQLSelectItem> items,List<ScistorSelectColumn> selectedColumns) throws ScistorParserException{
		if(items==null||items.isEmpty()) return;
		for(int i = 0;i<items.size();i++){
			SQLSelectItem item = items.get(i);
			ArrayList<ScistorSelectColumn> subColumns = new ArrayList<ScistorSelectColumn>();
			String alias = item.getAlias();
			ScistorSelectColumn column = new ScistorSelectColumn();
			column.setAlias(alias);
			subColumns.add(column);
			SQLExpr expr = item.getExpr();
			if(expr instanceof SQLNumericLiteralExpr == false){
				//modify by cx 20171217:if������Ϊ�෴�ģ�ȥ���쳣�����������
				//throw new ScistorParserException("SQL ERROR : "+expr.toString()+" is not supported syntax in select condition.");
				parseSelectExpr(expr, subColumns);
				for(int j=0;j<subColumns.size();j++){
					ScistorSelectColumn subColumn = subColumns.get(j);
					if(subColumn.getName() != null){
						selectedColumns.add(subColumn);
					}
				}
 			}
		}
	}
	
	/*
	 * ���� select ������
	 */
	protected void parseSelectExpr(SQLExpr expr,List<ScistorSelectColumn> subColumns) throws ScistorParserException{
		int index = subColumns.size()-1;
		ScistorSelectColumn column = subColumns.get(index);
		if(expr instanceof SQLAllColumnExpr){
			column.setName("*");
		}else if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			column.setName(e.getName());
		}else if(expr instanceof SQLPropertyExpr){
			SQLPropertyExpr e = (SQLPropertyExpr)expr;
			column.setName(e.getName());
			column.setOwner(e.getOwner().toString());
		}else if(expr instanceof SQLAggregateExpr){
			//��Ϊ�ۺϺ����ͷ���������ʱ��
			SQLAggregateExpr e = (SQLAggregateExpr)expr;
			column.setAggregator(true);
			
			SQLExpr aexpr = e.getArguments().get(0);
			if(aexpr instanceof SQLNumericLiteralExpr||aexpr instanceof SQLIntegerExpr) {
				//throw new ScistorParserException("SQL ERROR : "+aexpr.toString()+" is not supported syntax in "+e.getMethodName());
			    return;
			}
			parseSelectExpr(aexpr, subColumns);
			
			if(e.getOver() != null){
				if(e.getOver().getOrderBy()!=null){
					SQLOrderBy orderBy = (SQLOrderBy) e.getOver().getOrderBy();
					List<SQLSelectOrderByItem> items = orderBy.getItems();
					for(int i=0;i<items.size();i++){
						subColumns.add(new ScistorSelectColumn());
						parseSelectExpr(items.get(i).getExpr(),subColumns);
					}

				}
				
				ArrayList<SQLExpr> partitionByList = (ArrayList<SQLExpr>) e.getOver().getPartitionBy();
				for(int i = 0;i<partitionByList.size();i++){
					subColumns.add(new ScistorSelectColumn());
					parseSelectExpr(partitionByList.get(i),subColumns);
				}
			}
			
			if(e.getKeep() != null) {
				SQLOrderBy orderBy = (SQLOrderBy) e.getKeep().getOrderBy();
				List<SQLSelectOrderByItem> items = orderBy.getItems();
				for(int i=0;i<items.size();i++){
					subColumns.add(new ScistorSelectColumn());
					parseSelectExpr(items.get(i).getExpr(),subColumns);
				}
			}
			
		}else if(expr instanceof SQLMethodInvokeExpr){
			SQLMethodInvokeExpr e = (SQLMethodInvokeExpr) expr;
			//�������ۺϺ����ͷ�������
			if(e.getMethodName().toUpperCase().equals("DECODE")||
					e.getMethodName().toUpperCase().equals("NVL")||
					e.getMethodName().toUpperCase().equals("NVL2")||
					e.getMethodName().toUpperCase().equals("TRIM")||
					e.getMethodName().toUpperCase().equals("SUBSTR")||
					e.getMethodName().toUpperCase().equals("INSTR")||
					e.getMethodName().toUpperCase().equals("REPLACE")||
					e.getMethodName().toUpperCase().equals("LENGTH")||
					e.getMethodName().toUpperCase().equals("LPAD")||
					e.getMethodName().toUpperCase().equals("RPAD")||
					e.getMethodName().toUpperCase().equals("TO_DATE")||
					e.getMethodName().toUpperCase().equals("TO_CHAR")){
				List<SQLExpr> exprList = (List<SQLExpr>)e.getParameters();
				for(int i=0;i<exprList.size();i++){
					SQLExpr subExpr = exprList.get(i);
					if(i>0){
						subColumns.add(new ScistorSelectColumn());
					}
					parseSelectExpr(subExpr,subColumns);
				} 
			}
			//throw new ScistorParserException("SQL ERROR : function '"+e.getMethodName()+"' is not supported in select condition");
		}else if(expr instanceof SQLIntegerExpr||expr instanceof SQLNumericLiteralExpr){
			return;
		}else if(expr instanceof SQLBinaryOpExpr){
			SQLBinaryOpExpr e = (SQLBinaryOpExpr) expr;
			parseSelectExpr(e.getRight(),subColumns);
			subColumns.add(new ScistorSelectColumn());
			parseSelectExpr(e.getLeft(),subColumns);
			//delete by cx 20171217
			//throw new ScistorParserException("SQL ERROR : operator '"+e.getOperator().getName()+"' is not supported in select condition");
		}
	}
	
	/*
	 * ����where�����е��ֶ�
	 */
	protected void parseWhere(SQLExpr where,List<ScistorColumn> whereColumns,List<SQLSelectQuery> subQuerys) throws ScistorParserException{
		if(where==null) return;
		if (where instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr) where;
			String op = expr.getOperator().getName();
			if(op.equals("AND")||op.equals("OR")||!isValueOperator(op)){
				parseWhere(expr.getLeft(), whereColumns,subQuerys);
				parseWhere(expr.getRight(), whereColumns,subQuerys);
			}else{
				parseWhereBinaryExpr(expr,whereColumns);
			}
		} else if(where instanceof SQLIdentifierExpr){
			SQLIdentifierExpr expr = (SQLIdentifierExpr)where;
			String name = expr.getName();
			ScistorColumn column = new ScistorColumn(name);
			whereColumns.add(column);
		} else if(where instanceof SQLPropertyExpr){
			SQLPropertyExpr expr = (SQLPropertyExpr) where;
			String owner = expr.getOwner().toString();
			String columnName = expr.getName();
			ScistorColumn column = new ScistorColumn(owner,columnName);
			whereColumns.add(column);
		} else if (where instanceof SQLInListExpr) {
			SQLInListExpr expr = (SQLInListExpr) where;
			parseWhereInListExpr(expr,whereColumns);
		} else if (where instanceof SQLExistsExpr) {
			SQLExistsExpr expr = (SQLExistsExpr) where;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
		} else if (where instanceof SQLNotExpr) {
			SQLNotExpr expr = (SQLNotExpr) where;
			parseWhere(expr.getExpr(),whereColumns,subQuerys);
		} else if (where instanceof SQLBetweenExpr) {
			SQLBetweenExpr expr = (SQLBetweenExpr) where;
			parseWhereBetweenExpr(expr,whereColumns);
		} else if (where instanceof SQLInSubQueryExpr){
			SQLInSubQueryExpr expr  = (SQLInSubQueryExpr) where;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
			parseWhere(expr.getExpr(),whereColumns, null);
		} else if (where instanceof SQLAggregateExpr) {
			SQLAggregateExpr expr = (SQLAggregateExpr) where;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' can not be used in where conditon");
		} else if (where instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr) where;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' is not supported in where conditon");
		} 
	}
	/*
	 * ������    cname='123' ���͵�������ѯʱ
	 */
	protected void parseWhereBinaryExpr(SQLBinaryOpExpr expr,List<ScistorColumn> whereColumns) throws ScistorParserException{
		String op = expr.getOperator().getName();
		SQLExpr left = expr.getLeft();
		SQLExpr right = expr.getRight();
		if((left instanceof SQLIdentifierExpr || left instanceof SQLPropertyExpr)
				&&(right instanceof SQLTextLiteralExpr)){
			ScistorTextColumn column = new ScistorTextColumn();
			column.addExpr(right);
			if(op.equals("LIKE")||op.equals("NOT LIKE")){
				column.setLike(true);
			}else if(op.equals("REGEXP")||op.equals("NOT REGEXP")){
				column.setRegex(true);
			}
			parseWhereTextExpr(left, column);
			whereColumns.add(column);
		}else{
			parseHaving(left,whereColumns, null);
			parseHaving(right, whereColumns,null);
		}
	}
	
	/*
	 * ������  cname in (value1,value2); Oracle�� value1��value2���������Ϳ��Բ�һ��
	 */
	protected void parseWhereInListExpr(SQLInListExpr expr,List<ScistorColumn> whereColumns) throws ScistorParserException{
		List<SQLExpr> lists = expr.getTargetList();
		SQLExpr co = expr.getExpr();
		boolean hasTextValue = false;
		for(SQLExpr ee : lists){
			if(ee instanceof SQLTextLiteralExpr){
				hasTextValue = true;
				break;
			}
		}
		if(hasTextValue){
			ScistorTextColumn column = new ScistorTextColumn();
			parseWhereTextExpr(co, column);
			for(SQLExpr ee : lists){
				if(ee instanceof SQLTextLiteralExpr){
					column.addExpr(ee);
				}
			}
			whereColumns.add(column);
		}else{
			parseWhere(co,whereColumns, null);
		}
	}
	
	/*
	 * ����between... and ...
	 */
	protected void parseWhereBetweenExpr(SQLBetweenExpr expr,List<ScistorColumn> whereColumns) throws ScistorParserException{
		SQLExpr columnexpr = expr.getTestExpr();
		SQLExpr begin = expr.getBeginExpr();
		SQLExpr end = expr.getEndExpr();
		if((begin instanceof SQLTextLiteralExpr)&&(end instanceof SQLNumericLiteralExpr)){
			throw new ScistorParserException("SQL ERROR: between syntax "+begin.toString()+" and "+end.toString()+" is not same data type");
		}
		if((end instanceof SQLTextLiteralExpr)&&(begin instanceof SQLNumericLiteralExpr)){
			throw new ScistorParserException("SQL ERROR: between syntax "+begin.toString()+" and "+end.toString()+" is not same data type");
		}
		if((begin instanceof SQLTextLiteralExpr) && (end instanceof SQLTextLiteralExpr)){
			ScistorTextColumn column = new ScistorTextColumn();
			column.addExpr(begin);
			column.addExpr(end);
			parseWhereTextExpr(columnexpr, column);
			whereColumns.add(column);
		}else{
			parseWhere(columnexpr,whereColumns, null);
		}
	}
	
	/*
	 * ����where�к��� �ַ��������� ����
	 */
	protected void parseWhereTextExpr(SQLExpr expr,ScistorColumn column) throws ScistorParserException{
		if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			column.setName(e.getName());
		} else if(expr instanceof SQLPropertyExpr){
			SQLPropertyExpr e = (SQLPropertyExpr)expr;
			column.setName(e.getName());
			column.setOwner(e.getOwner().toString());
		} else if(expr instanceof SQLMethodInvokeExpr){
			SQLMethodInvokeExpr e = (SQLMethodInvokeExpr) expr;
			throw new ScistorParserException("SQL ERROR : function '"+e.getMethodName()+"' is not supported within char type column");
		} else if(expr instanceof SQLBinaryOpExpr){
			SQLBinaryOpExpr e = (SQLBinaryOpExpr) expr;
			throw new ScistorParserException("SQL ERROR : operator '"+e.getOperator().getName()+"' is not supported within char type column");
		} else if (expr instanceof SQLNotExpr) {
			SQLNotExpr e = (SQLNotExpr) expr;
			parseWhereTextExpr(e.getExpr(), column);
		} else if (expr instanceof SQLAggregateExpr) {
			SQLAggregateExpr e = (SQLAggregateExpr) expr;
			throw new ScistorParserException("SQL ERROR:'"+e.getMethodName()+"' is not supported within char type column");
		} 
	}
	
	protected boolean isValueOperator(String op){
		if(op.equals("=")
				||op.equals("!=")
				||op.equals("<>")
				||op.equals(">")
				||op.equals(">=")
				||op.equals("!>")
				||op.equals("<")
				||op.equals("<=")
				||op.equals("!<")
				||op.equals("<=>")
				||op.equals("<>")
				||op.equals("LIKE")
				||op.equals("NOT LIKE")
				||op.equals("REGEXP")
				||op.equals("NOT REGEXP")){
			return true;
		}
		return false;
	}
	
	/**
	 * ����join�����
	 * @param tableSource
	 * @param subQuerys
	 * @param tables
	 * @param onColumns 
	 * @throws ScistorParserException
	 */
	protected void parseJoinTableSource(SQLTableSource tableSource,Map<String,SQLSelectQuery> subQuerys,
			List<ScistorTable> tables, List<ScistorColumn> onColumns) throws ScistorParserException {
		if(tableSource instanceof SQLExprTableSource){
			SQLExprTableSource expr = (SQLExprTableSource) tableSource;
			SQLExpr tableExpr = expr.getExpr();
			if(!(tableExpr instanceof SQLIdentifierExpr)){
				throw new ScistorParserException("SQL ERROR : not supported table syntax "+tableExpr.toString());
			}
			String tablename = tableExpr.toString();
			String tablealias = expr.getAlias();
			ScistorTable table = new ScistorTable(tablename, tablealias);
			tables.add(table);
		}else if(tableSource instanceof SQLJoinTableSource){
			SQLJoinTableSource expr = (SQLJoinTableSource) tableSource;
			SQLTableSource left = expr.getLeft();
			SQLTableSource right = expr.getRight();
			parseJoinOnCondition(expr.getCondition(), onColumns);
			parseJoinUsingCondition(expr.getUsing(),onColumns);
			parseJoinTableSource(left,subQuerys,tables,onColumns);
			parseJoinTableSource(right,subQuerys,tables,onColumns);
		}else if(tableSource instanceof SQLSubqueryTableSource){
			SQLSubqueryTableSource expr = (SQLSubqueryTableSource) tableSource;
			subQuerys.put(expr.getAlias(), expr.getSelect().getQuery());//SQLSelectQuery
		}else if(tableSource instanceof SQLUnionQueryTableSource){
			SQLUnionQueryTableSource expr = (SQLUnionQueryTableSource) tableSource;
			subQuerys.put(expr.getAlias(), expr.getUnion());//SQLUnionQuery
		}else {
			throw new ScistorParserException("SQL ERROR : '"+tableSource.getClass().toString().substring(tableSource.getClass().toString().lastIndexOf("."))+"' is not supported");
		}
	}
	
	protected void parseJoinOnCondition(SQLExpr onCondition,List<ScistorColumn> onColumns) throws ScistorParserException{
		if(onCondition == null) return;
		if (onCondition instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr) onCondition;
			String op = expr.getOperator().getName();
			if(op.equals("AND")||op.equals("OR")||!isValueOperator(op)){
				parseJoinOnCondition(expr.getLeft(), onColumns);
				parseJoinOnCondition(expr.getRight(), onColumns);
			}else{
				parseWhereBinaryExpr(expr,onColumns);
			}
		} else if(onCondition instanceof SQLIdentifierExpr){
			SQLIdentifierExpr expr = (SQLIdentifierExpr)onCondition;
			String name = expr.getName();
			ScistorColumn column = new ScistorColumn(name);
			onColumns.add(column);
		} else if(onCondition instanceof SQLPropertyExpr){
			SQLPropertyExpr expr = (SQLPropertyExpr) onCondition;
			String owner = expr.getOwner().toString();
			String columnName = expr.getName();
			ScistorColumn column = new ScistorColumn(owner,columnName);
			onColumns.add(column);
		} else if (onCondition instanceof SQLInListExpr) {
			throw new ScistorParserException("SQL ERROR: 'in' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLExistsExpr) {
			throw new ScistorParserException("SQL ERROR: 'exists' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLNotExpr) {
			throw new ScistorParserException("SQL ERROR: 'not' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLBetweenExpr) {
			throw new ScistorParserException("SQL ERROR: 'between .. and ..' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLInSubQueryExpr){
			throw new ScistorParserException("SQL ERROR: 'in' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLAggregateExpr) {
			SQLAggregateExpr expr = (SQLAggregateExpr) onCondition;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr) onCondition;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' syntax is not supported within join on conditon");
		} 
	}
	
	private void parseJoinUsingCondition(List<SQLExpr> using, List<ScistorColumn> onColumns) throws ScistorParserException {
		if(using==null || using.size()==0) return;
		SQLExpr expr = using.get(0);
		if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			String name = e.getName();
			ScistorColumn column = new ScistorColumn(name);
			onColumns.add(column);
		}else{
			throw new ScistorParserException("SQL ERROR : not supported syntax in using condition");
		}
	}
	
	protected void parseGroupBy(SQLSelectGroupByClause groupby , List<ScistorColumn> groupbyColumns) throws ScistorParserException{
		if(groupby == null) return;
		List<SQLExpr> exprs = groupby.getItems();
		for(SQLExpr e : exprs){
			//OracleSelectGroupByExpr expr = (OracleSelectGroupByExpr) e;
			ScistorColumn column = new ScistorColumn();
			List<ScistorColumn> columnList = new ArrayList<ScistorColumn>();
			columnList.add(column);
			parseNameExpr(e, columnList, "group by");
			for(ScistorColumn subColumn:columnList) {
				if(subColumn.getName() != null) {
					groupbyColumns.add(subColumn);
				}
			}
		}
		SQLExpr having = groupby.getHaving();
		parseHaving(having, groupbyColumns, null);
	}
	
	
	protected void parseHaving(SQLExpr having , List<ScistorColumn> havingColumns,List<SQLSelectQuery> subQuerys) throws ScistorParserException{
		if(having==null) return;
		if (having instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr) having;
			String op = expr.getOperator().getName();
			if(op.equals("AND")||op.equals("OR")||!isValueOperator(op)){
				parseHaving(expr.getLeft(), havingColumns,subQuerys);
				parseHaving(expr.getRight(), havingColumns,subQuerys);
			}else{
				parseWhereBinaryExpr(expr,havingColumns);
			}
		} else if(having instanceof SQLIdentifierExpr){
			SQLIdentifierExpr expr = (SQLIdentifierExpr)having;
			String name = expr.getName();
			ScistorColumn column = new ScistorColumn(name);
			havingColumns.add(column);
		} else if(having instanceof SQLPropertyExpr){
			SQLPropertyExpr expr = (SQLPropertyExpr) having;
			String owner = expr.getOwner().toString();
			String columnName = expr.getName();
			ScistorColumn column = new ScistorColumn(owner,columnName);
			havingColumns.add(column);
		} else if (having instanceof SQLInListExpr) {
			SQLInListExpr expr = (SQLInListExpr) having;
			parseWhereInListExpr(expr,havingColumns);
		} else if (having instanceof SQLExistsExpr) {
			SQLExistsExpr expr = (SQLExistsExpr) having;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
		} else if (having instanceof SQLNotExpr) {
			SQLNotExpr expr = (SQLNotExpr) having;
			parseHaving(expr.getExpr(),havingColumns,subQuerys);
		} else if (having instanceof SQLBetweenExpr) {
			SQLBetweenExpr expr = (SQLBetweenExpr) having;
			parseWhereBetweenExpr(expr,havingColumns);
		} else if (having instanceof SQLInSubQueryExpr){
			SQLInSubQueryExpr expr  = (SQLInSubQueryExpr) having;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
			parseHaving(expr,havingColumns, null);
		} else if (having instanceof SQLAggregateExpr) {
			SQLAggregateExpr expr = (SQLAggregateExpr) having;
			SQLExpr col = expr.getArguments().get(0);
			ScistorColumn column = new ScistorColumn();
			List<ScistorColumn> columnList = new ArrayList<ScistorColumn>();
			columnList.add(column);
			parseNameExpr(col, columnList, "having "+expr.getMethodName());
			for(ScistorColumn subColumn:columnList) {
				if(subColumn.getName() != null) {
					havingColumns.add(subColumn);
				}
			}
			
		} else if (having instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr) having;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' is not supported in having conditon");
		} 
	}
	
	protected void parseOrderBy(SQLOrderBy orderby,List<ScistorColumn> orderbyColumns) throws ScistorParserException{
		if(orderby == null) return;
		List<SQLSelectOrderByItem> items = orderby.getItems();
		for(SQLSelectOrderByItem item : items){
			SQLExpr expr = item.getExpr();
			List<ScistorColumn> columnList = new ArrayList<ScistorColumn>();
			ScistorColumn column = new ScistorColumn();
			columnList.add(column);
			parseNameExpr(expr, columnList, "order by");
			for(int i=0;i<columnList.size();i++) {
				if(columnList.get(i).getName() != null) {
					orderbyColumns.add(columnList.get(i));
				}
			}
		}
	}
	
	protected void parseNameExpr(SQLExpr expr,List<ScistorColumn> columnList,String param) throws ScistorParserException{
		int index = columnList.size()-1;
		ScistorColumn column = columnList.get(index);
		if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			column.setName(e.getName());
		}else if(expr instanceof SQLPropertyExpr){
			SQLPropertyExpr e = (SQLPropertyExpr)expr;
			column.setName(e.getName());
			column.setOwner(e.getOwner().toString());
		}else if(expr instanceof SQLAggregateExpr) {
			SQLAggregateExpr e = (SQLAggregateExpr)expr;
		
			SQLExpr aexpr = e.getArguments().get(0);
			if(aexpr instanceof SQLNumericLiteralExpr||aexpr instanceof SQLIntegerExpr) {
				//throw new ScistorParserException("SQL ERROR : "+aexpr.toString()+" is not supported syntax in "+e.getMethodName());
			    return;
			}
			parseNameExpr(aexpr, columnList,param);
			
			if(e.getOver() != null){
				if(e.getOver().getOrderBy()!=null){
					SQLOrderBy orderBy = (SQLOrderBy) e.getOver().getOrderBy();
					List<SQLSelectOrderByItem> items = orderBy.getItems();
					for(int i=0;i<items.size();i++){
						columnList.add(new ScistorColumn());
						parseNameExpr(items.get(i).getExpr(),columnList,param);
					}

				}
				
				ArrayList<SQLExpr> partitionByList = (ArrayList<SQLExpr>) e.getOver().getPartitionBy();
				for(int i = 0;i<partitionByList.size();i++){
					columnList.add(new ScistorColumn());
					parseNameExpr(partitionByList.get(i),columnList,param);
				}
			}
			
			if(e.getKeep() != null) {
				SQLOrderBy orderBy = (SQLOrderBy) e.getKeep().getOrderBy();
				List<SQLSelectOrderByItem> items = orderBy.getItems();
				for(int i=0;i<items.size();i++){
					columnList.add(new ScistorColumn());
					parseNameExpr(items.get(i).getExpr(),columnList,param);
				}
			}
		}else if(expr instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr e = (SQLMethodInvokeExpr) expr;
			if(e.getMethodName().toUpperCase().equals("DECODE")||
					e.getMethodName().toUpperCase().equals("NVL")||
					e.getMethodName().toUpperCase().equals("NVL2")||
					e.getMethodName().toUpperCase().equals("TRIM")||
					e.getMethodName().toUpperCase().equals("SUBSTR")||
					e.getMethodName().toUpperCase().equals("INSTR")||
					e.getMethodName().toUpperCase().equals("REPLACE")||
					e.getMethodName().toUpperCase().equals("LENGTH")||
					e.getMethodName().toUpperCase().equals("LPAD")||
					e.getMethodName().toUpperCase().equals("RPAD")||
					e.getMethodName().toUpperCase().equals("TO_DATE")||
					e.getMethodName().toUpperCase().equals("TO_CHAR")){
				List<SQLExpr> exprList = (List<SQLExpr>)e.getParameters();
				for(int i=0;i<exprList.size();i++){
					SQLExpr subExpr = exprList.get(i);
					if(i>0){
						columnList.add(new ScistorColumn());
					}
					parseNameExpr(subExpr,columnList,param);
				} 
			}
		}/*else{
			if(!param.equals("order by")) {
				throw new ScistorParserException("SQL ERROR: "+expr.toString()+" is not supported syntax in "+param+" conditon");
			}
		}*/
	}
	/**
	 * ��ʱjoin�����ʱ��������ܶ�Ӧ�ı�
	 * @param column
	 * @param subSelectColumns
	 * @param tables
	 * @throws ScistorParserException 
	 */
	protected void findColumnTable(ScistorColumn column,Map<String,List<ScistorSelectColumn>> subSelectColumns,List<ScistorTable> tables) throws ScistorParserException{
		if(column.getOwner()==null){
			if(subSelectColumns==null){
				if(tables.size()==1){
					column.setOwner(tables.get(0).getTablename());
				}else{
					for(ScistorTable table : tables){
						column.addPossibleOwner(table.getTablename());
					}
				}
			}else{
				if(!inSubSelect(column,subSelectColumns)){
					if(tables.size()==1){
						column.setOwner(tables.get(0).getTablename());
					}else{
						for(ScistorTable table : tables){
							column.addPossibleOwner(table.getTablename());
						}
					}
				}
			}
		}else{
			for(ScistorTable table : tables){
				if(table.getTablealias()!=null){
					if(table.getTablealias().equals(column.getOwner())){
						column.setOwner(table.getTablename());
						break;
					}
				}
			}
		}
	}
	
	/**
	 * �ж��������Ƿ�������Ƕ�ײ�ѯ
	 * @param column
	 * @param subSelectColumns
	 * @return
	 * @throws ScistorParserException 
	 */
	protected boolean inSubSelect(ScistorColumn column, Map<String, List<ScistorSelectColumn>> subSelectColumns) throws ScistorParserException {
		for(String alias : subSelectColumns.keySet()){
			List<ScistorSelectColumn> lists = subSelectColumns.get(alias);
			for(ScistorSelectColumn in : lists){
				if(in.isSelectAll()) throw new ScistorParserException("SQL ERROR : not supported '*' in subquery or union clause");
				if(in.getAlias()!=null){
					if(in.getAlias().equals(column.getName())){
						return true;
					}
				}else{
					if(in.getName().equals(column.getName())){
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * ��ȡ��������Ƕ�ײ�ѯ�ı���
	 */
	protected void getInSubSelectAliases(ScistorColumn column, Map<String, List<ScistorSelectColumn>> subSelectColumns,List<String> inaliasNames) throws ScistorParserException {
		for(String alias : subSelectColumns.keySet()){
			List<ScistorSelectColumn> lists = subSelectColumns.get(alias);
			for(ScistorSelectColumn in : lists){
				if(in.isSelectAll()) throw new ScistorParserException("SQL ERROR : not supported '*' in subquery or union clause");
				if(in.getAlias()!=null){
					if(in.getAlias().equals(column.getName())){
						inaliasNames.add(alias);
					}
				}else{
					if(in.getName().equals(column.getName())){
						inaliasNames.add(alias);
					}
				}
			}
		}
	}
	
	protected List<ScistorSelectColumn> getSubQuerySelectColumns(String subAlias,SQLSelectQuery subQuery) throws ScistorParserException {
		if(subQuery instanceof OracleSelectQueryBlock){
			OracleSelectQueryBlock queryBlock = (OracleSelectQueryBlock) subQuery;
			List<ScistorSelectColumn> inselectedColumns = new ArrayList<ScistorSelectColumn>();
			List<SQLSelectItem> items = queryBlock.getSelectList();
			parseSelectItems(items, inselectedColumns);
			for(ScistorSelectColumn column : inselectedColumns){
				column.setSubQueryAlias(subAlias);
			}
			return inselectedColumns;
		}//else if(subQuery instanceof OracleUnionQuery){
		else if (subQuery instanceof SQLUnionQuery) {
			//OracleUnionQuery union = (OracleUnionQuery) subQuery;
			SQLUnionQuery union = (SQLUnionQuery)subQuery;
			/*
			 * ��ʱû�д���
			 */
			SQLSelectQuery  left = union.getLeft();
			while(!(left instanceof OracleSelectQueryBlock)){
				/*if(left instanceof OracleUnionQuery){
					left = ((OracleUnionQuery)left).getLeft();
				}*/
				if(left instanceof SQLUnionQuery){
					left = ((SQLUnionQuery)left).getLeft();
				}
			}
			return getSubQuerySelectColumns(subAlias,left);
		}
		return null;
	}
	
	protected void getQueryListFromUnion(SQLSelectQuery query,List<SQLSelectQuery> queryLists){
		if(query instanceof OracleSelectQueryBlock){
			queryLists.add(query);
		}//else if(query instanceof OracleUnionQuery){
		else if(query instanceof SQLUnionQuery){
			SQLUnionQuery union = (SQLUnionQuery) query;
			getQueryListFromUnion(union.getLeft(), queryLists);
			getQueryListFromUnion(union.getRight(), queryLists);
		}
	}
}
