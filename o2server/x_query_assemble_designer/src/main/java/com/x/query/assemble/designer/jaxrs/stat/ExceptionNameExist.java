package com.x.query.assemble.designer.jaxrs.stat;

import com.x.base.core.project.exception.LanguagePromptException;
import com.x.base.core.project.exception.PromptException;

class ExceptionNameExist extends LanguagePromptException {

	private static final long serialVersionUID = -9089355008820123519L;

	ExceptionNameExist(String str) {
		super("名称:{},已存在.", str);
	}
}
