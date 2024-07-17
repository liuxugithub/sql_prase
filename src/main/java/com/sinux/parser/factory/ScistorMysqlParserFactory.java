package com.sinux.parser.factory;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.sinux.parser.mysql.ScistorMysqlAlterParser;
import com.sinux.parser.mysql.ScistorMysqlCreateParser;
import com.sinux.parser.mysql.ScistorMysqlDeleteParser;
import com.sinux.parser.mysql.ScistorMysqlDropParser;
import com.sinux.parser.mysql.ScistorMysqlInsertParser;
import com.sinux.parser.mysql.ScistorMysqlParser;
import com.sinux.parser.mysql.ScistorMysqlSelectParser;
import com.sinux.parser.mysql.ScistorMysqlUpdateParser;
import com.sinux.parser.mysql.ScistorOtherParser;

/**
 * Mysql��������
 * @author GuoLiang
 */
public class ScistorMysqlParserFactory implements ScistorParserFactory{

	@Override
	public ScistorMysqlParser createSelectParser(SQLStatement statement) {
		return new ScistorMysqlSelectParser(statement);
	}

	@Override
	public ScistorMysqlParser createInsertParser(SQLStatement statement) {
		return new ScistorMysqlInsertParser(statement);
	}

	@Override
	public ScistorMysqlParser createUpdateParser(SQLStatement statement) {
		return new ScistorMysqlUpdateParser(statement);
	}

	@Override
	public ScistorMysqlParser createDeleteParser(SQLStatement statement) {
		return new ScistorMysqlDeleteParser(statement);
	}

	@Override
	public ScistorMysqlParser createAlterParser(SQLStatement statement) {
		return new ScistorMysqlAlterParser(statement);
	}

	@Override
	public ScistorMysqlParser createCreateParser(SQLStatement statement) {
		
		return new ScistorMysqlCreateParser(statement);
	}

	@Override
	public ScistorMysqlParser createDropParser(SQLStatement statement) {
		return new ScistorMysqlDropParser(statement);
	}

	@Override
	public ScistorMysqlParser createOtherParser(SQLStatement statement) {
		return new ScistorOtherParser(statement);
	}

}
