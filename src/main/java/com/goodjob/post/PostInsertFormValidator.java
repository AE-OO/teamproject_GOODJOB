package com.goodjob.post;

import com.goodjob.post.postdto.PostInsertDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class PostInsertFormValidator implements ConstraintValidator<PostInsertForm, PostInsertDTO> {
    @Override
    public void initialize(PostInsertForm constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
    @Override
    public boolean isValid(PostInsertDTO value, ConstraintValidatorContext context) {
        int invalidCount = 0;
        if(value.getPostStartDate()!=null) {
            if (value.getPostStartDate().getTime() > value.getPostEndDate().getTime()) {
                addConstraintViolation
                        (context, "The start date must be before the end date.", "postStartDate");
                invalidCount += 1;
            }
        } else {
            addConstraintViolation(context,"Null value","postStartDate");
            invalidCount += 1;
        }
        if(value.getPostEndDate()!=null) {
            if (value.getPostStartDate().getTime() > value.getPostEndDate().getTime()) {
                addConstraintViolation(context, "The end date must be after the start date.", "postEndDate");
                invalidCount += 1;
            }
        } else {
            addConstraintViolation(context,"Null value","postEndDate");
            invalidCount += 1;
        }
        if(value.getPostOccCode()<0 || value.getPostOccCode()>64){
            addConstraintViolation(context,"Invalid value","postOccCode");
            invalidCount +=1;
        }
        if(value.getPostSalaryId()<0 || value.getPostSalaryId()>14){
            addConstraintViolation(context,"Invalid value","postSalaryId");
            invalidCount +=1;
        }
        if(value.getPostOccCode()==0){
            addConstraintViolation(context,"Not Selected","postOccCode");
            invalidCount +=1;
        }
        if(value.getPostSalaryId()==0){
            addConstraintViolation(context,"Not Selected","postSalaryId");
            invalidCount +=1;
        }
        if(!Arrays.asList(new String[]{"남자", "여자","성별무관"}).contains(value.getPostGender())){
            addConstraintViolation(context, "Gender values must be either male, female, or gender-independent","postGender");
            invalidCount += 1;
        }

        if(value.getPostImg().get(0).isEmpty() && value.getPostImg().get(0).getSize()==0){
            addConstraintViolation(context, "Empty Attachments","postImg");
            invalidCount += 1;
        }
        return invalidCount == 0;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage, String firstNode) {
        context.disableDefaultConstraintViolation(); //
        context.buildConstraintViolationWithTemplate(errorMessage)
                .addPropertyNode(firstNode)
                .addConstraintViolation();
    }
}
