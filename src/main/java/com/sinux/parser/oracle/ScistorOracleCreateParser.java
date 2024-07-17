package com.sinux.parser.oracle;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateTableStatement;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorCreateResult;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.result.ScistorSQLType;

public class ScistorOracleCreateParser extends ScistorOracleParser{
	public ScistorOracleCreateParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorCreateResult();
		this.result.setSqlType(ScistorSQLType.CREATE);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		OracleCreateTableStatement create = (OracleCreateTableStatement) this.statement;
		String tablename = create.getName().getSimpleName();
		((ScistorCreateResult)this.result).setTableName(tablename);
		if(create.getSelect()!=null){
			throw new ScistorParserException("SQL ERROR : create table with select not support.");
		}
		return this.result;
	}
}
