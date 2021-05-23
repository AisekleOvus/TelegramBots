package finca;

import java.time.LocalDateTime;
import java.util.Objects;

public class SingleEvent {
	private Boolean isHappend;
	private LocalDateTime dateTime;
	private String country;
	private String caption;
	private String currentValue;
	private String previousValue;
	private String outlookValue;
	private String level;
	
    public SingleEvent(LocalDateTime dateTime, String country, String caption, String currentValue, String previousValue, String outlookValue, String level, Boolean isHappend) {
    	this.dateTime = dateTime;
    	this.country = country;
    	this.caption = caption;
    	this.currentValue = currentValue;
    	this.previousValue = previousValue;
    	this.outlookValue = outlookValue;
    	this.level = level;
    	this.isHappend = isHappend;
    }
    public void setIsHappend(boolean happening) {
    	isHappend = happening;
    }
	public void setCurrentValue(String curVal) {
		currentValue = curVal;
	}
	public void setPreviousValue(String prevVal) {
		previousValue = prevVal;
	}
	public void setOutlookValue(String outVal) {
		outlookValue = outVal;
	}
	public void setLevel(String lev) {
		level = lev;
	}
    public LocalDateTime getDateTime() {
    	return dateTime;
    }
    public String getCountry() {
    	return country;
    }
    public String getCaption() {
    	return caption;
    }
    public String getCurrentValue() {
    	return currentValue;
    }
    public String getPreviousValue() {
    	return previousValue;
    }
    public String getOutlookValue() {
    	return outlookValue;
    }
    public Boolean getIsHappend() {
    	return isHappend;
    }
    public String getLevel() {
    	return level;
    }
    
    @Override
    public boolean equals(Object o) { 
        if (o == this)
            return true; 
        if (!(o instanceof SingleEvent)) 
            return false; 

        SingleEvent se = (SingleEvent) o; 
        return dateTime.isEqual(se.getDateTime()) && caption.equals(se.getCaption());
    }
    @Override
    public int hashCode() {
    	return Objects.hash(dateTime, caption);
    }
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(dateTime.toString() + " ");
    	sb.append(country + " ");
    	sb.append(caption + " ");
    	sb.append(currentValue + " ");
    	sb.append(previousValue + " ");
    	sb.append(outlookValue + " ");
    	sb.append(level + " ");
    	sb.append(isHappend);
    	return sb.toString();
    }
}
