package com.hssc.ems;

import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.ioc.provider.ComboIocProvider;

import com.hssc.ems.open.SetupClass;

@Modules(scanPackage = true)
@IocBy(type = ComboIocProvider.class, args = { "*org.nutz.ioc.loader.json.JsonLoader", "ioc/", "*org.nutz.ioc.loader.annotation.AnnotationIocLoader","com.hssc.ems.action","com.hssc.ems.interchange" })
@SetupBy(SetupClass.class)
public class Module {
}
