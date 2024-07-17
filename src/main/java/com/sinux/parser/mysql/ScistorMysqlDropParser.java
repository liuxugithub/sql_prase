package com.sinux.parser.mysql;

import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.sinux.parser.result.ScistorDropResult;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.result.ScistorSQLType;

public class ScistorMysqlDropParser extends ScistorMysqlParser{

	public ScistorMysqlDropParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorDropResult();
		this.result.setSqlType(ScistorSQLType.DROP);
	}

	@Override
	public ScistorResult getParseResult() {
		SQLDropTableStatement drop = (SQLDropTableStatement)this.statement;
		List<SQLExprTableSource> tables = drop.getTableSources();
		for(SQLExprTableSource table : tables){
			((ScistorDropResult)this.result).addTableName(table.getExpr().toString());
		}
		return this.result;
	}
}
