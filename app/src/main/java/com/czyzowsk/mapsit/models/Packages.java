package com.czyzowsk.mapsit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Packages {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("receiver_phone")
    @Expose
    private long receiverPhone;
    @SerializedName("from_x_pos")
    @Expose
    private float fromXPos;
    @SerializedName("from_y_pos")
    @Expose
    private float fromYPos;
    @SerializedName("to_x_pos")
    @Expose
    private float toXPos;
    @SerializedName("to_y_pos")
    @Expose
    private float toYPos;
    @SerializedName("date_from")
    @Expose
    private String dateFrom;
    @SerializedName("date_to")
    @Expose
    private String dateTo;
    @SerializedName("max_deliver_time_minutes")
    @Expose
    private int maxDeliverTimeMinutes;
    @SerializedName("proposed_price")
    @Expose
    private int proposedPrice;
    @SerializedName("weight")
    @Expose
    private int weight;
    @SerializedName("height")
    @Expose
    private int height;
    @SerializedName("width")
    @Expose
    private int width;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("deliver_by_user_id")
    @Expose
    private String deliverByUserId;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("__v")
    @Expose
    private int v;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(long receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public float getFromXPos() {
        return fromXPos;
    }

    public void setFromXPos(float fromXPos) {
        this.fromXPos = fromXPos;
    }

    public float getFromYPos() {
        return fromYPos;
    }

    public void setFromYPos(float fromYPos) {
        this.fromYPos = fromYPos;
    }

    public float getToXPos() {
        return toXPos;
    }

    public void setToXPos(float toXPos) {
        this.toXPos = toXPos;
    }

    public float getToYPos() {
        return toYPos;
    }

    public void setToYPos(float toYPos) {
        this.toYPos = toYPos;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public int getMaxDeliverTimeMinutes() {
        return maxDeliverTimeMinutes;
    }

    public void setMaxDeliverTimeMinutes(int maxDeliverTimeMinutes) {
        this.maxDeliverTimeMinutes = maxDeliverTimeMinutes;
    }

    public int getProposedPrice() {
        return proposedPrice;
    }

    public void setProposedPrice(int proposedPrice) {
        this.proposedPrice = proposedPrice;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeliverByUserId() {
        return deliverByUserId;
    }

    public void setDeliverByUserId(String deliverByUserId) {
        this.deliverByUserId = deliverByUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

}
