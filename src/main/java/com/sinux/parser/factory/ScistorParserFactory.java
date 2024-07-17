package com.sinux.parser.factory;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.sinux.parser.ScistorParser;

/**
 * �ӿ�
 * @author GuoLiang
 */
public interface ScistorParserFactory {
	
	public ScistorParser createSelectParser(SQLStatement statement);
	public ScistorParser createInsertParser(SQLStatement statement);
	public ScistorParser createUpdateParser(SQLStatement statement);
	public ScistorParser createDeleteParser(SQLStatement statement);
	public ScistorParser createAlterParser(SQLStatement statement);
	public ScistorParser createCreateParser(SQLStatement statement);
	public ScistorParser createDropParser(SQLStatement statement);
	public ScistorParser createOtherParser(SQLStatement statement);
}
