package com.hssc.ems.action;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import com.hssc.ems.interchange.RegularTask;
import com.hssc.ems.pojo.Power;
import com.hssc.ems.pojo.Record;
import com.hssc.ems.pojo.Sensor;
import com.hssc.ems.pojo.User;
import com.hssc.ems.pojo.Warning;

@IocBean
@At("/machineRoom")
public class ComputerMachineRoomAction {

	private static final Log log = Logs.getLog(ComputerMachineRoomAction.class);

	@Inject
	private Dao dao;

	/**
	 * 用户列表
	 * 
	 * @return
	 */
	@At
	@Ok("raw:json")
	public Object userList(@Param("..") User user, int n, int s, HttpSession session) {
		Criteria cri = Cnd.cri();
		if (user.getUserName() != null) {
			cri.where().and("userName", "=", user.getUserName());
		}
		Pager pager = dao.createPager(n, s);
		List<User> list = dao.query(User.class, cri, pager);
		if (user.getPassword() != null && list.size() == 1) {
			String password = Lang.digest("MD5", user.getPassword());
			if (!list.get(0).getPassword().equals(password))
				list.get(0).setPassword(list.get(0).getPassword().equals(password) + "");
			else
				session.setAttribute("cmrUser", list.get(0));
		}
		pager.setRecordCount(dao.count(User.class, cri));

		log.warn("已登录");
		return Json.toJson(new QueryResult(list, pager));
	}

	/**
	 * 用户操作
	 * 
	 * @return
	 */
	@At
	@Ok("raw:html")
	public String editUser(@Param("..") User upUser, HttpSession session) {
		if (upUser.getRemark().equals("changePassword")) {
			User user = (User) session.getAttribute("cmrUser");
			String op = Lang.md5(Streams.wrap(Lang.toBytes(upUser.getUserName().toCharArray())));
			if (user != null && op.equals(user.getPassword())) {
				user.setPassword(Lang.md5(Streams.wrap(Lang.toBytes(upUser.getPassword().toCharArray()))));
				session.setAttribute("cmrUser", user);
				dao.update(user);
				return "密码修改成功";
			} else {
				return "密码修改不成功";
			}
		}
		String returnString = "";
		if (upUser.getId() != 0) {
			if (upUser.getRemark().equals("del")) {
				dao.delete(User.class, upUser.getId());
				returnString = "删除成功";
			} else {
				dao.update(upUser);
				returnString = "更新成功";
			}
		} else {
			upUser.setPassword(Lang.digest("MD5", upUser.getPassword()));
			returnString = "添加成功";
			if (dao.fetch(User.class, upUser.getUserName()) == null)
				dao.insert(upUser);
			else
				returnString = "重复添加";
		}
		return returnString;
	}

	/**
	 * 权限列表
	 * 
	 * @return
	 */
	@At
	@Ok("raw:json")
	public Object powerList(@Param("..") Power power, int n, int s) {
		Criteria cri = Cnd.cri();
		if (power.getName() != null) {
			cri.where().and("name", "=", power.getName());
		}
		Pager pager = dao.createPager(n, s);
		List<Power> list = dao.query(Power.class, cri, pager);
		pager.setRecordCount(dao.count(Power.class, cri));
		return Json.toJson(new QueryResult(list, pager), JsonFormat.full());
	}

	/**
	 * 权限操作
	 * 
	 * @return
	 */
	@At
	@Ok("raw:html")
	public String editPower(@Param("..") Power power) {
		String returnString = "";
		if (power.getId() != 0) {
			if (power.getRemark().equals("del")) {
				Criteria cri = Cnd.cri();
				cri.where().and("power", "=", power.getName());
				List<User> list = dao.query(User.class, cri);
				if (list.size() > 0) {
					returnString = "该权限下尚有使用用户，不可删除";
				} else {
					dao.delete(Power.class, power.getId());
					returnString = "删除成功";
				}
			} else {
				dao.update(power);
				returnString = "更新成功";
			}
		} else {
			dao.insert(power);
			returnString = "添加成功";
		}
		// String closeWin =
		// "parent.document.getElementsByClassName('div-resourceMyframe')[0].style.display='none';";
		return returnString;
	}

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

	/**
	 * 更改设备
	 * 
	 * @param sensor
	 * @return
	 */
	@At
	@Ok("raw:json")
	public String editSensor(@Param("..") Sensor sensor) {
		if (sensor.getType() != null && "区域".equals(sensor.getType())) {
			Criteria cri = Cnd.cri();
			// ???
			cri.where().and("type", "=", sensor.getType());
			List<Sensor> sl = dao.query(Sensor.class, cri);
			if (sl.size() == 0)
				dao.insert(sensor);
			else {
				Sensor s = sl.get(0);
				s.setContents(sensor.getContents());
				dao.update(s);
			}
		} else if (sensor.getId() != 0) {
			if (sensor.getRemark().equals("del")) {
				dao.delete(Sensor.class, sensor.getId());
			} else {
				dao.update(sensor);
			}
		} else {
			dao.insert(sensor);
		}
//		RegularTask.getInstance().getSensors();
		return Json.toJson(sensor, JsonFormat.full());
	}

	@At
	@Ok("raw:json")
	public Object warningList(@Param("..") Warning warning, int n, int s) {
		Criteria cri = Cnd.cri();
		if (warning.getRemark() != null && warning.getRemark().equals("getWarning()"))
			cri.where().and("status", "<>", "已处理");
		else
			cri.getOrderBy().desc("status");
		cri.getOrderBy().desc("warningTime");
		Pager pager = dao.createPager(n == 0 ? 1 : n, s);
		List<Warning> list = dao.query(Warning.class, cri, pager);
		pager.setRecordCount(dao.count(Warning.class, cri));
		return Json.toJson(new QueryResult(list, pager), JsonFormat.full());
	}

	@At
	@Ok("raw:json")
	public Object editWarning(@Param("..") Warning warning) {
		dao.update(warning);
		return Json.toJson(warning);
	}

	@At
	@Ok("raw:json")
	public String recordList(@Param("..") Record record, int n, int s, int sensorId) {
		Criteria cri = Cnd.cri();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, -600);
		if (sensorId != 0)
			cri.where().and("sensorId", "=", sensorId);
		cri.where().and("time", ">", c.getTime()).and("remark", "<>", "连接超时");
		cri.getOrderBy().desc("time");
		Pager pager = dao.createPager(n == 0 ? 1 : n, s);
		List<Record> list = dao.query(Record.class, cri, pager);
		pager.setRecordCount(dao.count(Record.class, cri));
		return Json.toJson(new QueryResult(list, pager));
	}

	@At
	@Ok("raw:json")
	public void sendOrder(int id,String comName) {
		//手动触发标记
		RegularTask.takeSensor(id, comName);
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

}
