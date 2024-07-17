package com.sinux.parser.result;

/**
 * SQL ����
 * @author GuoLiang
 */
public enum ScistorSQLType {
	SELECT(1),
	INSERT(2),
	UPDATE(3),
	DELETE(4),
	CREATE(5),
	ALTER(6),
	DROP(7),
	SHOW(8),
	OTHER(0);
	int type;
	ScistorSQLType(int type){
		this.type = type;
	}
	public int getType(){
		return this.type;
	}
}
