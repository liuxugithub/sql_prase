package com.sinux.parser.oracle;

import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleInsertStatement;
import com.sinux.parser.column.ScistorColumn;
import com.sinux.parser.column.ScistorTextColumn;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorInsertResult;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.result.ScistorSQLType;


public class ScistorOracleInsertParser extends ScistorOracleParser{
	public ScistorOracleInsertParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorInsertResult();
		this.result.setSqlType(ScistorSQLType.INSERT);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		OracleInsertStatement OracleInsert = (OracleInsertStatement) this.statement;
		List<SQLExpr> columnNames = OracleInsert.getColumns();
		String tablename = OracleInsert.getTableName().getSimpleName();
		((ScistorInsertResult)this.result).setTablename(tablename);
		if(columnNames.size()==0){
			((ScistorInsertResult)this.result).setNoColumn(true);
		}else{
			((ScistorInsertResult)this.result).setNoColumn(false);
			List<ValuesClause> valueLists = OracleInsert.getValuesList();
			int size = columnNames.size();
			for(int i = 0 ; i<size ; i++){
				SQLExpr columnName = columnNames.get(i);
				if(columnName instanceof SQLIdentifierExpr){
					String name = ((SQLIdentifierExpr)columnName).getName();
					ScistorColumn column = new ScistorTextColumn(name);
					((ScistorTextColumn)column).setOwner(tablename);
					for(ValuesClause clause : valueLists){
						((ScistorTextColumn)column).addExpr(clause.getValues().get(i));
					}
					((ScistorInsertResult)this.result).addConditionColumn(column);
				}
			}
		}
		
		return this.result;
	}
}
