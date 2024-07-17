package com.sinux.parser.factory;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.sinux.parser.oracle.ScistorOracleAlterParser;
import com.sinux.parser.oracle.ScistorOracleCreateParser;
import com.sinux.parser.oracle.ScistorOracleDeleteParser;
import com.sinux.parser.oracle.ScistorOracleDropParser;
import com.sinux.parser.oracle.ScistorOracleInsertParser;
import com.sinux.parser.oracle.ScistorOracleParser;
import com.sinux.parser.oracle.ScistorOracleSelectParser;
import com.sinux.parser.oracle.ScistorOracleUpdateParser;
import com.sinux.parser.oracle.ScistorOtherParser;

public class ScistorOracleParserFactory implements ScistorParserFactory{


		@Override
		public ScistorOracleParser createSelectParser(SQLStatement statement) {
			return new ScistorOracleSelectParser(statement);
		}

		@Override
		public ScistorOracleParser createInsertParser(SQLStatement statement) {
			return new ScistorOracleInsertParser(statement);
		}

		@Override
		public ScistorOracleParser createUpdateParser(SQLStatement statement) {
			return new ScistorOracleUpdateParser(statement);
		}

		@Override
		public ScistorOracleParser createDeleteParser(SQLStatement statement) {
			return new ScistorOracleDeleteParser(statement);
		}

		@Override
		public ScistorOracleParser createAlterParser(SQLStatement statement) {
			return new ScistorOracleAlterParser(statement);
		}

		@Override
		public ScistorOracleParser createCreateParser(SQLStatement statement) {
			
			return new ScistorOracleCreateParser(statement);
		}

		@Override
		public ScistorOracleParser createDropParser(SQLStatement statement) {
			return new ScistorOracleDropParser(statement);
		}

		@Override
		public ScistorOracleParser createOtherParser(SQLStatement statement) {
			return new ScistorOtherParser(statement);
		}

}
