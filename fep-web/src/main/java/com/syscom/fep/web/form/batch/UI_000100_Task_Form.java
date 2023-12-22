package com.syscom.fep.web.form.batch;

import java.util.HashMap;
import java.util.List;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;

public class UI_000100_Task_Form extends BaseForm {
	private static final long serialVersionUID = 1L;

	private Integer sender;

	private int tskId;

	private int jobId;

	private List<HashMap<String, Object>> tasks;

	private String jobsSeq;

	private String jobsJobID;

	private String taskID;

	public Integer getSender() {
		return sender;
	}

	public void setSender(Integer sender) {
		this.sender = sender;
	}

	public int getTskId() {
		return tskId;
	}

	public void setTskId(int tskId) {
		this.tskId = tskId;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public List<HashMap<String, Object>> getTasks() {
		return tasks;
	}

	public void setTasks(List<HashMap<String, Object>> tasks) {
		this.tasks = tasks;
	}

	public String getJobsSeq() {
		return jobsSeq;
	}

	public void setJobsSeq(String jobsSeq) {
		this.jobsSeq = jobsSeq;
	}

	public String getJobsJobID() {
		return jobsJobID;
	}

	public void setJobsJobID(String jobsJobID) {
		this.jobsJobID = jobsJobID;
	}

	public String getTaskID() {
		return taskID;
	}

	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}

	/**
	 * 執行結果
	 */
	@SuppressWarnings("unused")
	private boolean result = true;
	/**
	 * 訊息類別
	 */
	private MessageType messageType;
	/**
	 * 訊息內容
	 */
	private String message = "";

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
		if (messageType == MessageType.DANGER) {
			this.result = false;
		}
	}

	public void setMessage(MessageType messageType, String message) {
		this.setMessageType(messageType);
		this.setMessage(message);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
