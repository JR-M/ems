package com.hssc.ems.action;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

import com.hssc.ems.pojo.User;

@IocBean
@At("/t")
public class TempAction {
	
	@Inject
	private Dao dao;

	@At("/gt")
	@Ok("raw:json")
	public String getTime() {
		dao.create(User.class, false);
		return Json.toJson(Calendar.getInstance().getTime());
	}
	
	
	@At("/gu")
	@Ok("raw:json")
	public String getUser(int n, int s, Date startTime, Date endTime, String type){
		Criteria cri = Cnd.cri();
		cri.where();
		Pager pager = dao.createPager(n == 0 ? 1 : n, s);
		List<User> list = dao.query(User.class, cri, pager);
		pager.setRecordCount(dao.count(User.class, cri));
		return Json.toJson(new QueryResult(list, pager));
	}
	
	@At("/test")
	@Ok("raw:json")
	public String test(int t){
//		Sensor sensor = dao.fetch(Sensor.class, t);
//		 RegularTask.getInstance();
//		String s = RegularTask.getInstance().takeSensor(sensor);
		return "";
	}
	
}
