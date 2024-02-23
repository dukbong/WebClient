package com.webclient.test.wc.apicomponent;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubjectDivision {
	LIMS("LIMS", 8099),
	ELN("ELN", 8090),
	QMS("QMS", 8098),
	RDMS("RDMS", 8096);
	
	String subjectName;
	Integer portNum;
}