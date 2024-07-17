package com.sinux.parser.mysql;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorOtherResult;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.result.ScistorSQLType;

public class ScistorOtherParser  extends ScistorMysqlParser{

	
	public ScistorOtherParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorOtherResult();
		this.result.setSqlType(ScistorSQLType.OTHER);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		return null;
	}
}
