/**
 * 
 */

theMenu = {
	"name" : "主菜单",
	"children" : [ {
		"name" : "报警列表",
		"children" : [ {
			"name" : "报警列表 ",
			"links" : "../computer_machine_room/warning/warning_list.html"
		} ]
	}, {
		"name" : "系统管理 ",
		"children" : [ {
			"name" : "设备分布",
			"links" : "../computer_machine_room/room_sensor_edit.html"
		} ]
	}, {
		"name" : "权限管理",
		"children" : [ {
			"name" : "用户管理 ",
			"links" : "role/user.html"
		}, {
			"name" : "角色管理 ",
			"links" : "role/power.html"
		} ]
	}, {
		"name" : "短信息",
		"children" : [ {
			"name" : "发送记录 ",
			"links" : "../computer_machine_room/sms/sms_list.html"
		} ]
	}, {
		"name" : "配电系统",
		"children" : [ {
			"name" : "相电压",
			"links" : "../computer_machine_room/statistics_table.html?type=电量&remark=相电压"
		}, {
			"name" : "相电流",
			"links" : "../computer_machine_room/statistics_table.html?type=电量&remark=相电流"
		}, {
			"name" : "功率因素",
			"links" : "../computer_machine_room/statistics_table.html?type=电量&remark=功率因素"
		}, {
			"name" : "视在功率",
			"links" : "../computer_machine_room/statistics_table.html?type=电量&remark=视在功率"
		}, {
			"name" : "频率",
			"links" : "../computer_machine_room/statistics_table.html?type=电量&remark=频率"
		} ]
	}, {
		"name" : "UPS电源",
		"children" : [ {
			"name" : "内部整流器",
			"links" : "../computer_machine_room/statistics_table.html?type=内部整流器"
		}, {
			"name" : "逆变器",
			"links" : "../computer_machine_room/statistics_table.html?type=逆变器"
		}, {
			"name" : "电池电压/电池电流",
			"links" : "../computer_machine_room/statistics_table.html?type=电池电压/电池电流"
		}, {
			"name" : "旁路",
			"links" : "../computer_machine_room/statistics_table.html?type=旁路"
		}, {
			"name" : "负载",
			"links" : "../computer_machine_room/statistics_table.html?type=负载"
		} ]
	}, {
		"name" : "空调设备",
		"children" : [ {
			"name" : "运行状态",
			"links" : "../computer_machine_room/statistics_table.html?type=运行状态"
		} ]
	}, {
		"name" : "机房温湿度",
		"children" : [ {
			"name" : "温度",
			"links" : "../computer_machine_room/statistics_table.html?type=温度"
		}, {
			"name" : "湿度",
			"links" : "../computer_machine_room/statistics_table.html?type=湿度"
		} ]
	}, {
		"name" : "漏水检测",
		"children" : [ {
			"name" : "水浸检测",
			"links" : "../computer_machine_room/statistics_table.html?type=水浸"
		} ]
	}, {
		"name" : "烟雾报警",
		"children" : [ {
			"name" : "烟雾报警",
			"links" : "../computer_machine_room/statistics_table.html?type=烟感"
		} ]
	}, {
		"name" : "视频监控",
		"children" : [ {
			"name" : "运行状态",
			"links" : "../computer_machine_room/video.html"
		} ]
	}, {
		"name" : "门禁系统",
		"children" : [ {
			"name" : "运行状态",
			"links" : "../computer_machine_room/door_status.html?type=门禁系统"
		} ]
	} ]
}
