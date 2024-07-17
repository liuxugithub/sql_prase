package com.sinux.parser.mysql;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.sinux.parser.ScistorParser;
import com.sinux.parser.result.ScistorResult;

public abstract class ScistorMysqlParser implements ScistorParser{
	protected ScistorResult result;
	protected SQLStatement statement;
	
	public ScistorMysqlParser(SQLStatement statement){
		this.statement = statement;
	}
	
	
}
