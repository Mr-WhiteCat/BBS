package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.query.EmailCodeQuery;
import com.GPbbs.entity.po.EmailCode;
import com.GPbbs.entity.vo.PaginationResultVO;


/**
 * 
 * 邮箱验证码 业务接口
 * 
 */
public interface EmailCodeService {

	/**
	 * 根据条件查询列表
	 */
	List<EmailCode> findListByParam(EmailCodeQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(EmailCodeQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery param);

	/**
	 * 新增
	 */
	Integer add(EmailCode bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<EmailCode> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<EmailCode> listBean);

	/**
	 * 根据EmailAndCode查询对象
	 */
	EmailCode getEmailCodeByEmailAndCode(String email,String code);


	/**
	 * 根据EmailAndCode修改
	 */
	Integer updateEmailCodeByEmailAndCode(EmailCode bean,String email,String code);


	/**
	 * 根据EmailAndCode删除
	 */
	Integer deleteEmailCodeByEmailAndCode(String email,String code);

	/**
	 *
	 * @param email
	 * @param type
	 */
	void sendEmailCode(String email, Integer type);

	/**
	 * 邮箱验证码验证
	 * @param email
	 * @param code
	 */
	void checkCode(String email, String code);

}