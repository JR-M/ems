package com.hssc.ems.action.cptroom;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import com.hssc.ems.pojo.History;
import com.hssc.ems.pojo.Power;
import com.hssc.ems.pojo.Sensor;
import com.hssc.ems.pojo.User;

@IocBean
@At("/cptroom")
public class OneAction {
	
	private static final Log log = Logs.getLog(OneAction.class);
	
	@Inject
	private Dao dao;

	@At
	@Ok("raw:json")
	public String getUser(HttpSession session, String e) {
		if (e != null && e.equals("del")) {
			session.removeAttribute("cmrUser");
			return "{'message':'已退出登录'}";
		}
		User user = (User) session.getAttribute("cmrUser");
		Power power = null;
		if (user != null && user.getPower().length() > 0) {
			power = dao.fetch(Power.class, user.getPower());
		}
		Map<String, Object> hm = new HashMap<String, Object>();
		hm.put("user", user);
		hm.put("power", power);
		return Json.toJson(hm);
	}

	/**
	 * 设备列表
	 * 
	 * @return
	 */
	@At
	@Ok("raw:json")
	public String sensorList(@Param("..") Sensor sensor, int n, int s) {
		Criteria cri = Cnd.cri();
		if (sensor.getId() != 0)
			cri.where().and("id", "=", sensor.getId());
		Pager pager = dao.createPager(n == 0 ? 1 : n, s);
		List<Sensor> list = dao.query(Sensor.class, cri, pager);
		return Json.toJson(list, JsonFormat.full());
	}

	@At
	@Ok("raw:json")
	public String recordList(int n, int s, Date startTime, Date endTime, String type,String remark) {
		Criteria cri = Cnd.cri();
		type = (String) ((remark.equals(""))?type:remark);
		cri.where().andBetween("time", startTime, endTime).and("remark","LIKE", "%"+type+"%");
//		cri.where().andInBySql("sensorId", "SELECT id FROM com_room_sensor WHERE type LIKE '" + type + "%%'");
		cri.getOrderBy().desc("time");
		Pager pager = dao.createPager(n == 0 ? 1 : n, s);
		List<History> list = dao.query(History.class, cri, pager);
		pager.setRecordCount(dao.count(History.class, cri));
		return Json.toJson(new QueryResult(list, pager));

	}

	@At("/showHC")
	@Ok("raw:json")
	public Object showHighcharts(Date startTime, Date endTime, String type,String remark) {
		type = '%' + type + '%';
		remark = (String) ((remark==null)?'%':('%'+remark+'%'));
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sb.append("SELECT COUNT(id) AS c, IFNULL(MAX(number),0) AS max, IFNULL(MIN(number),0) AS min FROM com_room_history ");
		sb.append("WHERE remark LIKE @remark AND time BETWEEN @start AND @end ");
		sb.append("AND sensorId IN (SELECT id FROM com_room_sensor WHERE type LIKE @type )");
		Sql sql = Sqls.create(sb.toString());
		sql.params().set("remark", remark).set("start", tf.format(startTime)).set("end", tf.format(endTime)).set("type", type);
		sql.setCallback(new SqlCallback() {
			public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				while (rs.next()) {
					Map<String, Object> map = new HashMap<String, Object>();
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
						map.put(rs.getMetaData().getColumnName(i), rs.getString(i));
					list.add(map);
				}
				return list;
			}
		});
		dao.execute(sql);
		
		double max = Double.valueOf(sql.getList(Map.class).get(0).get("max").toString());
		double min = Double.valueOf(sql.getList(Map.class).get(0).get("min").toString());
		
		sb.delete(0, sb.length()).append("SELECT COUNT(id) AS id, FORMAT(AVG(number),1) AS n ,t FROM( SELECT id,number,CASE ");
		for (int i = 0; i < 10; i++) {
			double t = (max - min) / 10;
			sb.append("WHEN (number BETWEEN " + (min + i * t) + " AND " + (min + (i + 1) * t) + ") THEN '" + (min + i * t) + " ~ " + (min + (i + 1) * t) + "' ");
		}
		sb.append("ELSE 'other' END AS t FROM com_room_history WHERE remark LIKE @remark AND time BETWEEN @start AND @end ");
		sb.append("AND sensorId IN (SELECT id FROM com_room_sensor WHERE type LIKE @type) ) r GROUP BY r.t ");
		sql = Sqls.create(sb.toString());
		sql.params().set("remark",remark).set("start", tf.format(startTime)).set("end", tf.format(endTime)).set("type", type);
		sql.setCallback(new SqlCallback() {
			public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				while (rs.next()) {
					Map<String, Object> map = new HashMap<String, Object>();
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
						map.put(rs.getMetaData().getColumnName(i), rs.getString(i));
					list.add(map);
				}
				return list;
			}
		});
		dao.execute(sql);
		
		@SuppressWarnings("rawtypes")
		List<Map> reOne = (sql.getList(Map.class));
		
		sb.delete(0, sb.length()).append("SELECT COUNT(id) AS id, FORMAT(AVG(number),1) AS n ,t FROM( SELECT id,number,CASE ");
		long diff = (endTime.getTime() - startTime.getTime()) / 10;
		for (int i = 0; i < 10; i++) {
			String tt = tf.format(startTime);
			sb.append("WHEN (time BETWEEN '" + tt + "' AND '");
			startTime.setTime(startTime.getTime() + diff);
			sb.append(tf.format(startTime) + "') THEN '" + tt + "' ");
		}
		startTime.setTime(endTime.getTime()-(diff*10));
		sb.append("ELSE 'other' END AS t FROM com_room_history WHERE remark LIKE @remark AND time BETWEEN '" + tf.format(startTime) + "' AND '");
		sb.append( tf.format(endTime) + "' AND sensorId IN (SELECT id FROM com_room_sensor WHERE type LIKE @type)  )r GROUP BY r.t ");
		sql = Sqls.create(sb.toString());
		sql.params().set("remark", remark).set("type", type);
		sql.setCallback(new SqlCallback() {
			@Override
			public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				while (rs.next()) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("ids",rs.getString("id"));
					map.put("number", rs.getString("n"));
					map.put("time", rs.getString("t"));
					list.add(map);
				}
				return list;
			}
		});
		dao.execute(sql);
		log.warn("分时，分值查询");
		return Json.toJson(new Object[]{sql.getList(String.class),reOne});
	}

	@At
	@Ok("raw:json")
	public String saveJson(String o) {
		Object obj = Json.fromJson(Lang.inr(o));
		String a = (String) ((Map<?, ?>) obj).get("req");
		System.out.println("ComputerMachineRoomAction.saveJson()" + a);
		String r = Json.toJson(obj);
		System.out.println("ComputerMachineRoomAction.saveJson()" + r);
		return r;

	}

	public static void main(String[] args) throws Exception {
		SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = tf.parse("2016-02-29 00:00:00");
		Date end = tf.parse("2016-03-01 00:00:00");

		long diff = (end.getTime() - start.getTime()) / 10;
		String s = "";
		s += "SELECT COUNT(id),AVG(number),t_c FROM( SELECT id,number,CASE \n";
		for (int i = 0; i < 10; i++) {
			String tt = tf.format(start);
			s += "WHEN (time BETWEEN '" + tt + "' AND '";
			start.setTime(start.getTime() + diff);
			s += tf.format(start) + "') THEN '" + tt + "' \n";
		}
		start.setTime(end.getTime()-(diff*10));
		s += "ELSE '太小' END AS t_c FROM com_room_record WHERE time BETWEEN '" + tf.format(start) + "' AND '" + tf.format(end) + "' \n";
		s += "AND sensorId LIKE 6 )t1 GROUP BY t1.t_c ";
		System.out.println("" + s);

	}

}
