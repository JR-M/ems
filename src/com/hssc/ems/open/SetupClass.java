package com.hssc.ems.open;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.Ioc;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

import com.hssc.ems.interchange.RegularTask;
import com.hssc.ems.pojo.DoorRecord;
import com.hssc.ems.pojo.History;
import com.hssc.ems.pojo.Power;
import com.hssc.ems.pojo.Record;
import com.hssc.ems.pojo.SMS;
import com.hssc.ems.pojo.Sensor;
import com.hssc.ems.pojo.User;
import com.hssc.ems.pojo.Warning;

public class SetupClass implements Setup {

	private static final Log log = Logs.getLog(SetupClass.class);

	public static Ioc ioc;
	
	private Dao dao;

	@Override
	public void destroy(NutConfig arg0) {
		// TODO Auto-generated method stub
		log.info("Nutz 正在关闭 " + Calendar.getInstance().getTime());
	}

	@Override
	public void init(NutConfig nutCfg) {
		// TODO Auto-generated method stub
		dao = nutCfg.getIoc().get(Dao.class);
		log.debug("SetupClass.SetupClass() 开始 " + Calendar.getInstance().getTime() + " " + nutCfg.getAppRoot());
		dao.create(Record.class, false);
		dao.create(History.class, false);
		dao.create(Warning.class, false);
		dao.create(Sensor.class, false);
		dao.create(User.class, false);
		dao.create(Power.class, false);
		dao.create(DoorRecord.class, false);
		dao.create(SMS.class, false);

		RegularTask.getInstance(dao);

		if (dao.query(User.class, null).size() < 1)
			initUser(nutCfg);
	}

	private void initUser(NutConfig nutCfg) {
		String menu = Files.read(nutCfg.getAppRoot() + "/computer_machine_room/data/system.js").replaceAll("(^[^\\=]*\\=)|(\\r\\n)|(\\s)", "");
		List<Object> l = new ArrayList<Object>();
		log.warn(menu);
		for (Object s : (List<?>) ((Map<?, ?>) Json.fromJson(Lang.inr(menu))).get("children"))
			for (Object i : (List<?>) ((Map<?, ?>) s).get("children"))
				l.add(i);
		menu = Json.toJson(l, JsonFormat.compact());
		log.info(menu);
		Power power = new Power();
		power.setContents(menu);
		power.setName("超级管理员");
		power.setRemark(Calendar.getInstance().getTime().toString());
		Cnd.cri();
		if (dao.query(Power.class, Cnd.where("name", "LIKE", power.getName())).size() == 1)
			dao.update(power);
		else
			dao.insert(power);
		User user = new User();
		user.setPower(power.getName());
		user.setUserName("管理员");
		user.setPassword(Lang.digest("MD5", "123456"));
		user.setStatus("已激活");
		user.setRemark(power.getRemark());
		if (dao.query(User.class, Cnd.where("userName", "LIKE", user.getUserName())).size() == 1)
			dao.update(user);
		else
			dao.insert(user);
	}

}
