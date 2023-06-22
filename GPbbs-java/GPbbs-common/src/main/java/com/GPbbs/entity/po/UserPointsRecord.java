package com.GPbbs.entity.po;

import com.GPbbs.entity.enums.UserPointsOperTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.GPbbs.entity.enums.DateTimePatternEnum;
import com.GPbbs.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 
 * 用户积分记录表
 * 
 */
public class UserPointsRecord implements Serializable {


	/**
	 * 记录ID
	 */
	private Integer recordId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 操作类型
	 */
	private Integer operType;

	/**
	 * 积分
	 */
	private Integer points;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	private String operTypeName;

	public String getOperTypeName() {
		UserPointsOperTypeEnum operTypeEnum = UserPointsOperTypeEnum.getByType(operType);
		return operTypeEnum == null ? "":operTypeEnum.getDesc();
	}

	public void setOperTypeName(String operTypeName) {
		this.operTypeName = operTypeName;
	}

	public void setRecordId(Integer recordId){
		this.recordId = recordId;
	}

	public Integer getRecordId(){
		return this.recordId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setOperType(Integer operType){
		this.operType = operType;
	}

	public Integer getOperType(){
		return this.operType;
	}

	public void setPoints(Integer points){
		this.points = points;
	}

	public Integer getPoints(){
		return this.points;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "记录ID:"+(recordId == null ? "空" : recordId)+"，用户ID:"+(userId == null ? "空" : userId)+"，操作类型:"+(operType == null ? "空" : operType)+"，积分:"+(points == null ? "空" : points)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
