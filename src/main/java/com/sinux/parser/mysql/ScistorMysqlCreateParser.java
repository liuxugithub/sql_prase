package com.sinux.parser.mysql;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorCreateResult;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.result.ScistorSQLType;

public class ScistorMysqlCreateParser extends ScistorMysqlParser{

	public ScistorMysqlCreateParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorCreateResult();
		this.result.setSqlType(ScistorSQLType.CREATE);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		MySqlCreateTableStatement create = (MySqlCreateTableStatement) this.statement;
		String tablename = create.getName().getSimpleName();
		((ScistorCreateResult)this.result).setTableName(tablename);
		if(create.getQuery()!=null){
			throw new ScistorParserException("SQL ERROR : create table with select not support.");
		}
		return this.result;
	}
	
}
