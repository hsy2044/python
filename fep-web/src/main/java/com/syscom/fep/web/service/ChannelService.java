package com.syscom.fep.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.model.Channel;

@Service
public class ChannelService extends BaseService{

	@Autowired
	private ChannelExtMapper channelExtMapper;
	
	/**
	 * Bruce add 取得通道下拉選單
	 * @return
	 * @throws Exception
	 */
	public List<Channel> queryAllData() throws Exception{
		try {
			return channelExtMapper.queryChannelOptions();
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
}
