package com.GPbbs.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.GPbbs.entity.config.WebConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.*;
import com.GPbbs.entity.query.*;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.mappers.*;
import com.GPbbs.service.*;
import com.GPbbs.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.vo.PaginationResultVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 
 * 用户信息 业务接口实现
 * 
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

	@Resource
	private UserInfoMapper<UserInfo,UserInfoQuery> userInfoMapper;

	@Resource
	private EmailCodeService emailCodeService;

	@Resource
	private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;

	@Resource
	private UserPointsRecordMapper<UserPointsRecord, UserPointsRecordQuery> userPointsRecordMapper;

	@Resource
	private WebConfig webConfig;

	@Resource
	private FileUtils fileUtils;

	@Resource
	private ForumCommentMapper<ForumComment, ForumCommentQuery> forumCommentMapper;


	@Resource
	private ForumArticleMapper<ForumArticle, ForumArticleQuery> forumArticleMapper;

	@Resource
	private UserMessageService userMessageService;



	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}


	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean){
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId){
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean,String userId){
		return this.userInfoMapper.updateByUserId(bean,userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId){
		return this.userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email){
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean,String email){
		return this.userInfoMapper.updateByEmail(bean,email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email){
		return this.userInfoMapper.deleteByEmail(email);
	}

	/**
	 * 根据NickName获取对象
	 */
	@Override
	public UserInfo getUserInfoByNickName(String nickName){
		return this.userInfoMapper.selectByNickName(nickName);
	}

	/**
	 * 根据NickName修改
	 */
	@Override
	public Integer updateUserInfoByNickName(UserInfo bean,String nickName){
		return this.userInfoMapper.updateByNickName(bean,nickName);
	}

	/**
	 * 根据NickName删除
	 */
	@Override
	public Integer deleteUserInfoByNickName(String nickName){
		return this.userInfoMapper.deleteByNickName(nickName);
	}




	/**
	 * 注册
	 * @param email
	 * @param emailCode
	 * @param nickName
	 * @param password
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String emailCode, String nickName, String password) {

		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);

		if (null != userInfo) {
			throw new BusinessException("邮箱账号已经存在");
		}

		UserInfo nickNameUser = this.userInfoMapper.selectByNickName(nickName);

		if (null != nickNameUser) {
			throw new BusinessException("昵称已经存在");
		}

		// 校验邮箱验证码
		emailCodeService.checkCode(email, emailCode);

		// 写入数据库
		// 生成随机数
		String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
		UserInfo insertInfo = new UserInfo();
		insertInfo.setUserId(userId);
		insertInfo.setNickName(nickName);
		insertInfo.setEmail(email);
		insertInfo.setPassword(StringTools.encodeByMD5(password));
		insertInfo.setJoinTime(new Date());
		insertInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		insertInfo.setTotalPoints(0);
		insertInfo.setCurrentPoints(0);
		this.userInfoMapper.insert(insertInfo);

		// 更新用户积分
		updateUserPoints(userId, UserPointsOperTypeEnum.REGISTER, UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.ADD.getChangeType(), Constants.POINTS_5);


		// 记录消息
		UserMessage userMessage = new UserMessage();
		userMessage.setReceivedUserId(userId);//设置接收消息用户id
		userMessage.setMessageType(MessageTypeEnum.SYS.getType());//设置系统消息种类
		userMessage.setCreateTime(new Date());
		userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());//设置消息已读未读
		userMessage.setMessageContent(SysCacheUtils.getSysSetting().getRegisterSetting().getRegisterWelcomInfo());
		userMessageMapper.insert(userMessage);
//		userMessageService.add(userMessage);

	}

	/**
	 * 更新用户积分
	 * @param userId
	 * @param operTypeEnum
	 * @param changeType
	 * @param points
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserPoints(String userId, UserPointsOperTypeEnum operTypeEnum, Integer changeType, Integer points) {
		points = changeType * points;

		if (points == 0) {
			return;
		}

		UserInfo userInfo = userInfoMapper.selectByUserId(userId);
		// 如果用户积分操作过后小于0 就将要操作积分设置为当前积分
		if (UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.REDUCE.getChangeType().equals(changeType) && userInfo.getCurrentPoints() + points < 0) {
			points = changeType * userInfo.getCurrentPoints();
		}

		UserPointsRecord record = new UserPointsRecord();
		record.setUserId(userId);
		record.setOperType(operTypeEnum.getOperType());
		record.setCreateTime(new Date());
		record.setPoints(points);
		this.userPointsRecordMapper.insert(record);

//		this.userInfoMapper.updatePoints(userId,points);

		// 类似于乐观锁
		Integer count = this.userInfoMapper.updatePoints(userId, points);

		if (count == 0) {
			throw new BusinessException("更新用户积分失败");
		}
	}

	/**
	 * 登录
	 * @param email
	 * @param password
	 * @param ip
	 * @return
	 */
	@Override
	public SessionWebUserDto login(String email, String password, String ip) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (null == userInfo || !userInfo.getPassword().equals(password)) {
			throw new BusinessException("账号或者密码错误");
		}
		if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
			throw new BusinessException("账号已禁用");
		}

		// 获取地域信息
		String ipAddress = getIpAddress(ip);



		// 更新用户信息
		UserInfo updateInfo = new UserInfo();
		updateInfo.setLastLoginTime(new Date());
		updateInfo.setLastLoginIp(ip);
		updateInfo.setLastLoginIpAddress(ipAddress);
		this.userInfoMapper.updateByUserId(updateInfo, userInfo.getUserId());

		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setNickName(userInfo.getNickName());
		sessionWebUserDto.setProvince(ipAddress);
		sessionWebUserDto.setUserId(userInfo.getUserId());
		if (!StringTools.isEmpty(webConfig.getAdminEmails()) && ArrayUtils.contains(webConfig.getAdminEmails().split(","), userInfo.getEmail())) {
			sessionWebUserDto.setAdmin(true);
		} else {
			sessionWebUserDto.setAdmin(false);
		}

		return sessionWebUserDto;
	}

	/**
	 * 获取用户IpAddress
	 * @param ip
	 * @return
	 */
	public String getIpAddress(String ip) {
		Map<String, String> addressInfo = new HashMap<>();
		// 获取ip地址信息
		try {
			String url = "http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip;
			String responseJson = OKHttpUtils.getRequest(url);
			if (StringTools.isEmpty(responseJson)) {
				return Constants.PRO_UNKNOWN;
			}
			addressInfo = JsonUtils.convertJson2Obj(responseJson, Map.class);
			if(ip.equals("127.0.0.1")){
				return "本机";
			}
			return addressInfo.get("pro");
		} catch (Exception e) {
			logger.error("获取ip所在地失败");
		}
		return Constants.PRO_UNKNOWN;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resetPwd(String email, String password, String emailCode) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (null == userInfo) {
			throw new BusinessException("邮箱账号不存在");
		}
		//校验邮箱验证码
		emailCodeService.checkCode(email, emailCode);

		UserInfo updateInfo = new UserInfo();
		// 设置新密码
		updateInfo.setPassword(StringTools.encodeByMD5(password));
		this.userInfoMapper.updateByEmail(updateInfo, email);
	}

	/**
	 * 更新用户信息
	 * @param userInfo
	 * @param avatar
	 */
	@Override
	public void updateUserInfo(UserInfo userInfo, MultipartFile avatar) {
		userInfoMapper.updateByUserId(userInfo,userInfo.getUserId());
		if(avatar!=null){
			fileUtils.uploadFile2Local(avatar, FileUploadTypeEnum.AVATAR,userInfo.getUserId());
		}

	}

	/**
	 * 根据用户id更新用户账号使用状态
	 * @param status
	 * @param userId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserStatus(Integer status, String userId) {
		if (UserStatusEnum.DISABLE.getStatus().equals(status)) {
			// 禁止屏蔽用户文章评论
			forumArticleMapper.updateStatusBatchByUserId(ArticleStatusEnum.DEL.getStatus(), userId);
			forumCommentMapper.updateStatusBatchByUserId(CommentStatusEnum.DEL.getStatus(), userId);
		} else{
			forumArticleMapper.updateStatusBatchByUserId(ArticleStatusEnum.AUDIT.getStatus(), userId);
			forumCommentMapper.updateStatusBatchByUserId(CommentStatusEnum.AUDIT.getStatus(), userId);
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setStatus(status);
		// 禁用账户 || 或者开启账户
		userInfoMapper.updateByUserId(userInfo, userId);

	}

	/**
	 * 发送消息，改变积分
	 * @param userId
	 * @param message
	 * @param points
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendMessage(String userId, String message, Integer points) {
		UserMessage userMessage = new UserMessage();
		userMessage.setReceivedUserId(userId);
		userMessage.setMessageType(MessageTypeEnum.SYS.getType());
		userMessage.setCreateTime(new Date());
		userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
		userMessage.setMessageContent(message);
		userMessageService.add(userMessage);

		UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum changeTypeEnum = UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.ADD;
		if (points != null && points != 0) {
			// 扣减积分传入正数
			if (points < 0) {
				points = points * -1;
				changeTypeEnum = UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.REDUCE;
			}
			updateUserPoints(userId, UserPointsOperTypeEnum.ADMIN, changeTypeEnum.getChangeType(), points);
		}
	}
}