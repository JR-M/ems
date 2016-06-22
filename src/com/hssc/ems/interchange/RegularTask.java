package com.hssc.ems.interchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Tasks;
import org.nutz.lang.socket.Sockets;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mapl.Mapl;

import com.hssc.ems.pojo.Record;
import com.hssc.ems.pojo.Sensor;
import com.hssc.ems.pojo.Warning;

public class RegularTask {

	private static final Log log = Logs.getLog(RegularTask.class);

	private static RegularTask instance;

	private Dao dao;

	static Stack<BinaryCommand> stack = new Stack<BinaryCommand>();

	static List<BinaryCommand> binaryCommands = new ArrayList<BinaryCommand>();

	static BinaryCommand warningLightOpen;

	static BinaryCommand warningLightOff;

	static Map<Integer, Stack<Float>> warnings = new HashMap<Integer, Stack<Float>>();

	public static byte[] crc16Modbus(byte[] b) {
		String hiS = "00C1814001C0804101C0804100C1814001C0804100C1814000C1814001C0804101C0804100C1814000C1814001C0804100C1814001C0804101C0804100C1814001C0804100C1814000C1814001C0804100C1814001C0804101C0804100C1814000C1814001C0804101C0804100C1814001C0804100C1814000C1814001C0804101C0804100C1814000C1814001C0804100C1814001C0804101C0804100C1814000C1814001C0804101C0804100C1814001C0804100C1814000C1814001C0804100C1814001C0804101C0804100C1814001C0804100C1814000C1814001C0804101C0804100C1814000C1814001C0804100C1814001C0804101C0804100C18140";
		String loS = "00C0C101C30302C2C60607C705C5C404CC0C0DCD0FCFCE0E0ACACB0BC90908C8D81819D91BDBDA1A1EDEDF1FDD1D1CDC14D4D515D71716D6D21213D311D1D010F03031F133F3F23236F6F737F53534F43CFCFD3DFF3F3EFEFA3A3BFB39F9F83828E8E929EB2B2AEAEE2E2FEF2DEDEC2CE42425E527E7E62622E2E323E12120E0A06061A163A3A26266A6A767A56564A46CACAD6DAF6F6EAEAA6A6BAB69A9A86878B8B979BB7B7ABABE7E7FBF7DBDBC7CB47475B577B7B67672B2B373B17170B0509091519353529296565797559594549C5C5D9D5F9F9E5E5A9A9B5B99595898884849894B8B8A4A4E8E8F4F8D4D4C8C44848545874746868242438341818040";
		byte[] t = new byte[b.length + 2];
		byte hi = (byte) 0x00FF, low = (byte) 0x00FF;
		for (int j, i = 0; i < b.length;) {
			t[i] = b[i];
			j = (low ^ t[i++]) & 0x00FF;
			low = (byte) (hi ^ Integer.valueOf(hiS.substring(2 * j, 2 * j + 2), 16).byteValue());
			hi = Integer.valueOf(loS.substring(2 * j, 2 * j + 2), 16).byteValue();
		}
		t[b.length] = low;
		t[t.length - 1] = hi;
		if (low == 0 && hi == 0)
			return b;
		return t;
	}

	public void initVariable() {
		for (Sensor sensor : dao.query(Sensor.class, Cnd.where("type", "NOT REGEXP", ".*像|区.*"))) {
			BinaryCommand binaryCommand = new BinaryCommand();
			binaryCommand.setSensorId(sensor.getId());
			Object cJson = Json.fromJson(Lang.inr(sensor.getContents()));
			binaryCommand.setIp((String) Mapl.cell(cJson, "ip"));
			binaryCommand.setPort(Integer.parseInt((String) Mapl.cell(cJson, "port")));
			@SuppressWarnings("rawtypes")
			ArrayList coms = (ArrayList) Mapl.toMaplist(Mapl.cell(cJson, "coms"));
			for (int i = 0; i < coms.size(); i++) {
				Object comObj = coms.get(i);
				String comName = comObj.toString().replaceAll("\\{([^\\=]+).*", "$1");
				Object command = Mapl.cell(comObj, comName);
				String range = (String) Mapl.cell(command, "range");
				String ask = (String) Mapl.cell(command, "ask");
				String feedback = (String) Mapl.cell(command, "feedback");
				if (i == 0) {
					binaryCommand.setCommandName(comName);
					binaryCommand.setRange(range.split(","));
					String[] arrayAsk = ask.split(",");
					for (int j = 0; j < arrayAsk.length; j++) {
						String[] tempS = arrayAsk[j].replaceAll("0x", "").split("\\s+");
						byte[] req = new byte[tempS.length];
						for (int k = 0; k < tempS.length; k++)
							req[k] = Integer.valueOf(tempS[k], 16).byteValue();
						if (!feedback.matches(".*A\\s.*"))
							req = crc16Modbus(req);
						List<byte[]> asks = binaryCommand.getAsk() == null ? (new ArrayList<byte[]>()) : binaryCommand.getAsk();
						asks.add(req);
						binaryCommand.setAsk(asks);
					}
					binaryCommand.setFeedback(feedback.split(","));
				} else {
					ArrayList<BinaryCommand> bcs = new ArrayList<BinaryCommand>();
					bcs = (ArrayList<BinaryCommand>) (binaryCommand.getBinaryCommands() == null ? bcs : binaryCommand.getBinaryCommands());
					BinaryCommand bc = new BinaryCommand();
					bc.setSensorId(sensor.getId());
					bc.setIp(binaryCommand.getIp());
					bc.setPort(binaryCommand.getPort());
					bc.setCommandName(comName);
					bc.setRange(range.split(","));
					String[] arrayAsk = ask.split(",");
					for (int j = 0; j < arrayAsk.length; j++) {
						String[] tempS = arrayAsk[j].replaceAll("0x", "").split("\\s+");
						byte[] req = new byte[tempS.length];
						for (int k = 0; k < tempS.length; k++)
							req[k] = Integer.valueOf(tempS[k], 16).byteValue();
						if (!feedback.matches(".*A\\s.*"))
							req = crc16Modbus(req);
						List<byte[]> asks = bc.getAsk() == null ? (new ArrayList<byte[]>()) : bc.getAsk();
						asks.add(req);
						bc.setAsk(asks);
					}
					bc.setFeedback(feedback.split(","));
					bcs.add(bc);
					if (comName.equals("开报警灯"))
						warningLightOpen = bc;
					if (comName.equals("关报警灯"))
						warningLightOff = bc;
					binaryCommand.setBinaryCommands(bcs);
				}
			}
			binaryCommands.add(binaryCommand);
			stack.push(binaryCommand);
		}
	}

	public void run() {
		System.currentTimeMillis();
		Tasks.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				while (!stack.isEmpty()) {
					BinaryCommand binarycommand = (BinaryCommand) stack.pop();
					BinaryCommand bc = setSocket(binarycommand);
					if (bc.getRep().get(0)[0] != 0)
						saveRecord(bc);
//					log.info(Json.toJson(bc));
				}
				for (BinaryCommand binarycommand : binaryCommands)
					stack.push(binarycommand);

				boolean light = dao.query(Record.class, Cnd.where("remark", "LIKE", "警灯").desc("time")).get(0).getNumber() == 1;
				List<Warning> ws = dao.query(Warning.class, Cnd.where("status", "NOT LIKE", "已处理"));

				if (!light && ws.size() > 0) {
					log.warn("要报警了");
					stack.push(warningLightOpen);
					new SMSTask(dao).sendSMS(ws);;
					
				}
				if (light && ws.size() == 0) {
					log.warn("关报警了");
					stack.push(warningLightOff);
				}

			}
		}, 1);

		Tasks.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				String s = "INSERT INTO com_room_history(sensorId,number,time,remark) ";
				s += "SELECT  sensorId, AVG(number) AS number, MIN(time) AS time, remark FROM com_room_record ";
				s += " GROUP BY sensorId, remark, DATE_FORMAT(time,'%Y-%m-%d %H') ORDER BY remark;";
				String d = " DELETE FROM com_room_record;";
				Sql sql = Sqls.create(s);
				dao.execute(sql);
				sql = Sqls.create(d);
				dao.execute(sql);
				log.warn("正在执行 清理数据库 ");
			}
		}, 1, 2, TimeUnit.HOURS);
		//

	}

	private RegularTask(Dao dao) {
		this.dao = dao;
		initVariable();
		run();
	}

	public static synchronized RegularTask getInstance(Dao dao) {
		if (instance == null)
			instance = new RegularTask(dao);
		return instance;
	}

	public static String takeSensor(int id, String comName) {
		for (BinaryCommand bc : binaryCommands)
			if (bc.getSensorId() == id)
				for (BinaryCommand bcInner : bc.getBinaryCommands())
					if (bcInner.getCommandName().equals(comName))
						stack.push(bcInner);
		// stack.push(s);
		// log.warn("已经压入," + Json.toJson(s));
		log.warn("触发传感器,id:" + id + " comName " + comName);
		return Calendar.getInstance().getTime() + " ";
	}

	private BinaryCommand setSocket(BinaryCommand bc) {
		byte[] b = new byte[256];
		List<byte[]> rep = new ArrayList<byte[]>();
		for (byte[] ask : bc.getAsk()) {
			try {
				Socket sk = new Socket();
				sk.setSoTimeout(1000);
				sk.connect(new InetSocketAddress(bc.getIp(), bc.getPort()), 1000);
				sk.getOutputStream().write(ask);
				sk.getInputStream().read(b);
				Sockets.safeClose(sk);
				
				log.debug(bc.getSensorId() + " : " + Lang.fixedHexString(b).toUpperCase().replaceAll("(\\w{2})", "$0 "));
				
				if (b != crc16Modbus(b))
					throw new SocketTimeoutException();
			} catch (SocketTimeoutException e) {
				// log.error("RegularTask.setSocket() timeout 连接超时");
			} catch (IOException e) {
				// log.error("RegularTask.setSocket() IO异常 连接超时");
			}
			rep.add(b);
		}
		bc.setRep(rep);
		return bc;
	}

	boolean valid(int id, Float v) {
		Stack<Float> sk;
		if (warnings.containsKey(id)) {
			sk = warnings.get(id);
			if (Math.abs(sk.peek() - v) > (sk.peek() * 0.1)) {
				warnings.remove(id);
				return false;
			}
		} else {
			sk = new Stack<Float>();
		}
		if (sk.size() > 2) {
			warnings.remove(id);
			return true;
		}
		sk.push(v);
		warnings.put(id, sk);
		return false;
	}

	public void saveRecord(BinaryCommand bc) {
		List<java.lang.Float> recordValues = byteDecoding(bc);
		for (int j = 0; j < recordValues.size(); j++) {
			Float v = recordValues.get(j);
			Record record = new Record();
			record.setSensorId(bc.getSensorId());
			record.setNumber(v);
			record.setTime(Calendar.getInstance().getTime());
			record.setRemark(bc.getFeedback()[j].trim().split("\\s+")[2]);
			dao.insert(record);

			if (!bc.getRange()[0].trim().equals("")) {
				for (int i = 0; i < bc.getRange().length; i++) {
					String[] range = bc.getRange()[i].trim().split("\\s+");
					if (range[0].equals("<") && v < Integer.valueOf(range[1]) || range[0].equals(">") && v > Integer.valueOf(range[1])) {
						if (valid(bc.getSensorId(), v)) {
							Criteria cri = Cnd.cri();
							cri.where().and("sensorId", "=", bc.getSensorId()).and("warningType", "=", range[2]).and("status", "<>", "已处理");
							if (dao.query(Warning.class, cri).size() < 1) {
								Warning warning = new Warning();
								warning.setSensorId(bc.getSensorId());
								warning.setWarningTime(Calendar.getInstance().getTime());
								warning.setStatus("报警");
								warning.setWarningVal(v);
								warning.setWarningType(range[2]);
								dao.insert(warning);
							}
						}
					}
				}
			}
		}
	}

	List<Float> byteDecoding(BinaryCommand bc) {
		byte[] b = bc.getRep().get(0);
		List<Float> request = new ArrayList<Float>();
		for (String feedback : bc.getFeedback()) {
			String[] k = feedback.trim().split("\\s+");
			int i = Integer.valueOf(k[0]).intValue();
			switch (k[1].toCharArray()[0]) {
			case 'B':
				request.add(Float.intBitsToFloat(b[i] & 0xFF | (b[i + 1] & 0xFF) << 8 | (b[i + 2] & 0xFF) << 16 | (b[i + 3] & 0xFF) << 24));
				break;
			case 'D':
				request.add((float) (((b[i] & 0xFF) << 8 | b[i + 1] & 0xFF) / 100.0));
				break;
			case 'I':
				request.add(Float.valueOf(Integer.toBinaryString(b[3] >> i & 1)));
				break;
			case 'O':
				request.add(Float.valueOf(Integer.toBinaryString(b[3] >> i & 1)));
				break;
			case 'U':
				request.add(Float.intBitsToFloat(b[i + 3] & 0xFF | (b[i + 2] & 0xFF) << 8 | (b[i + 1] & 0xFF) << 16 | (b[i] & 0xFF) << 24));
				break;
			default:
				break;
			}
		}
		return request;
	}

}
