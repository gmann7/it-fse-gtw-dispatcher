package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AccreditamentoPrefixEnum {

	CRASH_TIMEOUT("CRASH_TIMEOUT"),
	SKIP_VALIDATION("SKIP_VALIDATION"),
	CRASH_WF_EDS("CRASH_WF_EDS"),
	CRASH_INI("CRASH_INI"),
	CRASH_EDS("CRASH_EDS");
	
	private String prefix;
	 
	
	public static AccreditamentoPrefixEnum get(String inPrefix) {
		AccreditamentoPrefixEnum out = null;
		for (AccreditamentoPrefixEnum v: AccreditamentoPrefixEnum.values()) {
			if (v.getPrefix().equalsIgnoreCase(inPrefix)) {
				out = v;
				break;
			}
		}
		return out;
	}

	public static AccreditamentoPrefixEnum getStartWith(String inPrefix) {
		AccreditamentoPrefixEnum out = null;
		for (AccreditamentoPrefixEnum v: AccreditamentoPrefixEnum.values()) {
			if(inPrefix.startsWith(v.getPrefix())) {
				out = v;
				break;
				}
			}
		return out;
	}

}