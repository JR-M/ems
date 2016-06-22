package com.hssc.ems.action;

import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import com.hssc.ems.pojo.SMS;

@IocBean
@At("/sms")
public class SMSAction {

	@Inject
	private Dao dao;

	@At
	@Ok("raw:json")
	public Object smsList(@Param("..") SMS sms, int n, int s) {
		Criteria cri = Cnd.cri();
		if (sms.getContents() != null) {
			cri.where().and("contents", "LIKE", sms.getContents());
		}
		Pager pager = dao.createPager(n, s);
		List<SMS> list = dao.query(SMS.class, cri, pager);
		pager.setRecordCount(dao.count(SMS.class, cri));
		return Json.toJson(new QueryResult(list, pager), JsonFormat.full());
	}
}
