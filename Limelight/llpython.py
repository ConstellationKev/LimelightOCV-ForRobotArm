import cv2
import numpy as np
    
# runPipeline() is called every frame by Limelight's backend.
def runPipeline(image, llrobot):
    # converting to hsv colorspace
    img_hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    largestContour = np.array([[]])
    llpython = [0,0,0,0,0,0,0,0]
    filtered_contours = []

    # Yellow ------------
    # defining mask
    lower_y = (20, 90, 90)
    upper_y = (30, 255, 255)
    yellow_mask = cv2.inRange(img_hsv, lower_y, upper_y)
    # creating masks
    kernal = np.ones((9, 9), "uint8") # dilating amount
    yellow_mask = cv2.dilate(yellow_mask, kernal)
    res_yellow = cv2.bitwise_and(image, image, mask=yellow_mask)
    # creating contours
    y_contours, _ = cv2.findContours(yellow_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    for _, contour in enumerate(y_contours):
        area = cv2.contourArea(contour)
        if area > 10000:
            filtered_contours.append(contour)

    # Other Color -----------
    if llrobot[0] == 0.0: 
        # red
        lower_other = (160, 110, 110)
        upper_other = (180, 255, 255)
    elif llrobot[0] == 1.0:
        # blue
        lower_other = (90,50,70)
        upper_other = (128,255,255)
    other_mask = cv2.inRange(img_hsv, lower_other, upper_other)
    # creating masks
    kernal = np.ones((9, 9), "uint8") # dilating amount
    other_mask = cv2.dilate(other_mask, kernal)
    res_other = cv2.bitwise_and(image, image, mask=other_mask)
    # creating contours
    o_contours, _ = cv2.findContours(other_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    for _, contour in enumerate(o_contours):
        area = cv2.contourArea(contour)
        if area > 10000:
            filtered_contours.append(contour)

    # finding biggest contour
    if len(filtered_contours) > 0:
        cv2.drawContours(image, filtered_contours, -1, 255, 2)
        largestContour = max(filtered_contours, key=cv2.contourArea)
        x,y,w,h = cv2.boundingRect(largestContour)
        _,_,rotation = cv2.minAreaRect(largestContour)
        cv2.rectangle(image,(x,y),(x+w,y+h),(0,255,255),2)
        llpython = [rotation,x,y,w,h,-1,-1,-1]  

    # make sure to return a contour,
    # an image to stream,
    # and optionally an array of up to 8 values for the "llpython"
    # networktables array
    return largestContour, image, llpython