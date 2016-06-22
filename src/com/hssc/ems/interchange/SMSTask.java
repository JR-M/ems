package com.hssc.ems.interchange;

import java.util.Calendar;
import java.util.List;


import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.hssc.ems.pojo.SMS;
import com.hssc.ems.pojo.User;
import com.hssc.ems.pojo.Warning;

public class SMSTask {

	private static final Log log = Logs.getLog(SMSTask.class);

	Dao dao;

	public SMSTask(Dao dao) {
		this.dao = dao;
	}

	// 编辑短信接收人
	static List<User> users;

	// 发送短信
	public void sendSMS(List<Warning> ws) {
		if(users==null)
			users = takeUsers();
		String contents = ">";
		for (Warning w:ws)
			contents += " 报警类型:" + w.getWarningType() + ";";
		
		for(User user:users){
			SMS sms = new SMS();
			sms.setTime(Calendar.getInstance().getTime());
			sms.setId(user.getId());
			sms.setContents(contents);
			log.warn(" 发送短信 到 :" + user.getUserName() + "(" + user.getPhone() + "),内容为:" + contents);
		}
		
	}

	// 获取有权限的用户
	public List<User> takeUsers() {
		Criteria cri = Cnd.cri();
		cri.where().andNotIsNull("phone").andLike("status", "已激活");
		cri.where().andInBySql("power", "SELECT name FROM com_room_power WHERE contents LIKE '%%发送记录%%'", "");
		return dao.query(User.class, cri, dao.createPager(1, 30));
	}

}
