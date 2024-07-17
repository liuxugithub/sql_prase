package com.sinux.parser.oracle;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.sinux.parser.ScistorParser;
import com.sinux.parser.result.ScistorResult;

public abstract class ScistorOracleParser implements ScistorParser{
	protected ScistorResult result;
	protected SQLStatement statement;
	
	public ScistorOracleParser(SQLStatement statement){
		this.statement = statement;
	}
	
}
