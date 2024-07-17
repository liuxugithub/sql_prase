package com.sinux.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.sinux.parser.authentication.ScistorAuthenticate;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.factory.ScistorMysqlParserFactory;
import com.sinux.parser.factory.ScistorParserFactory;
import com.sinux.parser.mysql.ScistorMysqlSelectParser;
import com.sinux.parser.result.ScistorResult;

/**
 * �������
 * @author GuoLiang
 */
public class ScistorSQLParser {
	
	private ScistorResult result;	
	private ScistorAuthenticate auth;	///��where�ļ��ܻ���������ʽ�ļ���ʵ������ӿڣ����ݽ�������м���
	private SQLStatement statement;
	private ScistorParser sparser;
	
	public ScistorSQLParser(String sql,ScistorAuthenticate authentication) throws ScistorParserException{
		this.auth = authentication;
		parse(sql);
	}
	public void addCondition(String left, String right, SQLBinaryOperator operator){
		if(sparser instanceof ScistorMysqlSelectParser){
			ScistorMysqlSelectParser mysqlSelectParser = (ScistorMysqlSelectParser) sparser;
			mysqlSelectParser.addCondition(left,right,operator);
		}
	}
	public void parse(String sql) throws ScistorParserException{
		//MySqlStatementParser parser = new MySqlStatementParser(sql);
		MySqlStatementParser parser = new MySqlStatementParser(sql);
		statement = parser.parseStatement();
		ScistorParserFactory factory = new ScistorMysqlParserFactory();
		//ScistorParserFactory factory = new ScistorMysqlParserFactory();
		sparser = null;
		if(statement instanceof SQLSelectStatement){
			sparser = factory.createSelectParser(statement);
		}
		/*else if(statement instanceof MySqlInsertStatement){
			sparser = factory.createInsertParser(statement);
		
		}else if(statement instanceof MySqlUpdateStatement){
			sparser = factory.createUpdateParser(statement);
		
		}else if(statement instanceof MySqlDeleteStatement){
			sparser = factory.createDeleteParser(statement);
		
		}else if(statement instanceof SQLAlterTableStatement){
			sparser = factory.createAlterParser(statement);
		
		}else if(statement instanceof MySqlCreateTableStatement){
			sparser = factory.createCreateParser(statement);
		
		}else if(statement instanceof SQLDropTableStatement){
			sparser = factory.createDropParser(statement);
			
		}else {
			sparser = factory.createOtherParser(statement);
		}*/
		
		this.result = sparser.getParseResult();
	}
	
	public ScistorParser getParser(){
		return this.sparser;
	}
	
	public ScistorAuthenticate getAuthenticate(){
		return this.auth;
	}
	public int getAuthResult(){
		return this.auth.auth(this.result);
	}
	
	public ScistorResult getResult(){
		return this.result;
	}
	
	public String getChangedSql() {
		return statement.toString();
	}
	
}
