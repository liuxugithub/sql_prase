package com.sinux.parser.result;

/**
 * @author GuoLiang
 */
public abstract class ScistorResult {
	
	protected ScistorSQLType sqlType ;
	
	public ScistorSQLType getSqlType() {
		return sqlType;
	}

	public void setSqlType(ScistorSQLType sqlType) {
		this.sqlType = sqlType;
	}
	
}
