package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.Eat;

public interface EatMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.EAT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.EAT
     *
     * @mbg.generated
     */
    int insert(Eat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.EAT
     *
     * @mbg.generated
     */
    int insertSelective(Eat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.EAT
     *
     * @mbg.generated
     */
    Eat selectByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.EAT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Eat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.EAT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Eat record);
}