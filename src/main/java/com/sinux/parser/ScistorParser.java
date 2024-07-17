package com.sinux.parser;

import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorResult;

/**
 * �����ӿ�
 * @author GuoLiang
 */
public interface ScistorParser {
	
	/**
	 * ��ý������
	 * @return ScistorResult
	 * @throws ScistorParserException
	 */
	public ScistorResult getParseResult() throws ScistorParserException;
	
}
