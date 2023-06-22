package com.GPbbs.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import com.GPbbs.entity.config.WebConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.SysSetting4EmailDto;
import com.GPbbs.entity.po.UserInfo;
import com.GPbbs.entity.query.UserInfoQuery;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.mappers.UserInfoMapper;
import com.GPbbs.utils.StringTools;
import com.GPbbs.utils.SysCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.enums.PageSize;
import com.GPbbs.entity.query.EmailCodeQuery;
import com.GPbbs.entity.po.EmailCode;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.query.SimplePage;
import com.GPbbs.mappers.EmailCodeMapper;
import com.GPbbs.service.EmailCodeService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * 邮箱验证码 业务接口实现
 * 
 */
@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {

	private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);


	@Resource
	private EmailCodeMapper<EmailCode,EmailCodeQuery> emailCodeMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private JavaMailSender javaMailSender;

	@Resource
	private WebConfig webConfig;



	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<EmailCode> findListByParam(EmailCodeQuery param) {
		return this.emailCodeMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(EmailCodeQuery param) {
		return this.emailCodeMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<EmailCode> list = this.findListByParam(param);
		PaginationResultVO<EmailCode> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(EmailCode bean){
		return this.emailCodeMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<EmailCode> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.emailCodeMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<EmailCode> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.emailCodeMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据EmailAndCode获取对象
	 */
	@Override
	public EmailCode getEmailCodeByEmailAndCode(String email,String code){
		return this.emailCodeMapper.selectByEmailAndCode(email,code);
	}

	/**
	 * 根据EmailAndCode修改
	 */
	@Override
	public Integer updateEmailCodeByEmailAndCode(EmailCode bean,String email,String code){
		return this.emailCodeMapper.updateByEmailAndCode(bean,email,code);
	}

	/**
	 * 根据EmailAndCode删除
	 */
	@Override
	public Integer deleteEmailCodeByEmailAndCode(String email,String code){
		return this.emailCodeMapper.deleteByEmailAndCode(email,code);
	}

	/**
	 * 发送邮件实体方法
	 * @param toEmail
	 * @param code
	 */
	private void sendEmailCodeDo(String toEmail, String code) {
		try {

			MimeMessage message = javaMailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			// 邮件发件人
			helper.setFrom(webConfig.getSendUserName());
			// 邮件收件人 1或多个
			helper.setTo(toEmail);

			SysSetting4EmailDto emailDto = SysCacheUtils.getSysSetting().getEmailSetting();

			// 邮件主题
//			helper.setSubject("注册邮箱验证码");
			helper.setSubject(emailDto.getEmailTitle());
			// 邮件内容
//			helper.setText("邮箱验证码为"+code);
			helper.setText(String.format(emailDto.getEmailContent(), code));
			// 邮件发送时间
			helper.setSentDate(new Date());
			javaMailSender.send(message);
		} catch (Exception e) {
			logger.error("邮件发送失败", e);
			throw new BusinessException("邮件发送失败");
		}
	}

	/**
	 * 对邮箱验证码进行验证
	 * 时间（15分钟） ...
	 * @param email
	 * @param code
	 */
	@Override
	public void checkCode(String email, String code) {


		EmailCode dbInfo = this.emailCodeMapper.selectByEmailAndCode(email, code);

		if (null == dbInfo) {
			throw new BusinessException("邮箱验证码不正确");
		}
		if (dbInfo.getStatus() == 1 || System.currentTimeMillis() - dbInfo.getCreateTime().getTime() > Constants.LENGTH_15 * 1000 * 60) {
			throw new BusinessException("邮箱验证码已失效");
		}
		emailCodeMapper.disableEmailCode(email);


	}

	/**
	 * 邮箱验证码，
	 * 如果已存在并且未使用，则改变状态
	 * 如果未存在，则添加记录并且状态设置未未使用
	 *
	 * @param email
	 * @param type
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendEmailCode(String email, Integer type) {

		// 如果是注册，校验邮箱是否已存在
		if (type == Constants.ZERO) {
			UserInfo userInfo = userInfoMapper.selectByEmail(email);
			if (null != userInfo) {
				throw new BusinessException("邮箱已经存在");
			}
		}

		// 生成随机字符串
		String code = StringTools.getRandomString(Constants.LENGTH_5);

		sendEmailCodeDo(email, code);

		/*if (webConfig.getSendEmailCode() != null && webConfig.getSendEmailCode()) {
			sendEmailCodeDo(email, code);
		}*/

		// 将改邮箱验证码可用状态设置为不可用
		emailCodeMapper.disableEmailCode(email);

		EmailCode emailCode = new EmailCode();
		emailCode.setCode(code);
		emailCode.setEmail(email);
		emailCode.setStatus(Constants.ZERO);
		emailCode.setCreateTime(new Date());
		emailCodeMapper.insert(emailCode);

	}
}