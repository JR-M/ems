package com.hssc.ems.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

import com.hssc.ems.pojo.DoorRecord;


@At("/door")
@IocBean
public class DoorAction {
	@Inject
	private Dao dao;
	
	@At
	@Ok("raw:json")
	public String recordList(int n, int s, String name, String card, Date starttime, Date endtime) {
		Criteria cri = Cnd.cri();
		SqlExpressionGroup seg = cri.where();
		if(!"".equals(name)) seg.and("b.username", "=", name);
		if(!"".equals(card)) seg.and("a.card","=",card);
		if(starttime!=null) seg.and("a.time", ">", starttime);
		if(endtime!=null) seg.and("a.time", "<", endtime);
		Sql sql1 = Sqls.queryString("SELECT count(*) s FROM com_door_record a left join com_room_user b on a.card=b.card "+ cri);
		dao.execute(sql1);
		Pager pager = dao.createPager(n==0?1:n, s);
		pager.setRecordCount(Integer.parseInt(sql1.getString()));
		Sql sql = Sqls.queryRecord("SELECT a.*,b.username FROM com_door_record a left join com_room_user b on a.card=b.card "+ cri +" ORDER BY a.time DESC LIMIT "+ (n==0?0:n-1)*10 +",10");
		dao.execute(sql);
		List<Record> list = sql.getList(Record.class);
		List<Object[]> obj = new ArrayList<Object[]>();
		for(int i=0; i<list.size(); i++) {
			Record r = list.get(i);
			DoorRecord dr = r.toEntity(dao.getEntity(DoorRecord.class));
			String username = r.getString("username");
			obj.add(new Object[]{username,dr});
		}
		return Json.toJson(new QueryResult(obj, pager));

	}
}

