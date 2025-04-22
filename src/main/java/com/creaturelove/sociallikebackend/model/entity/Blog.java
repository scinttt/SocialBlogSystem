package com.creaturelove.sociallikebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName blog
 */
@TableName(value ="blog")
@Data
public class Blog implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long userid;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String coverimg;

    /**
     * 
     */
    private String content;

    /**
     * 
     */
    private Integer thumbcount;

    /**
     * 
     */
    private Date createtime;

    /**
     * 
     */
    private Date updatetime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}