package org.example.mapper;

import org.example.model.Test;

public interface TestMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TEST
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TEST
     *
     * @mbg.generated
     */
    int insert(Test record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TEST
     *
     * @mbg.generated
     */
    int insertSelective(Test record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TEST
     *
     * @mbg.generated
     */
    Test selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TEST
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Test record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TEST
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Test record);
}