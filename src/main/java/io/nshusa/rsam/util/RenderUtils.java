package io.nshusa.rsam.util;

import io.nshusa.rsam.binary.RSFont;
import io.nshusa.rsam.binary.RSWidget;
import io.nshusa.rsam.binary.sprite.RSSprite;
import io.nshusa.rsam.graphics.render.RSRaster;

public class RenderUtils {

    private RenderUtils() {

    }

    public static void renderWidget(RSWidget widget, int x, int y, int scroll) {
        if (widget.group != 0 || widget.children == null) {
            return;
        }

        int clipLeft = RSRaster.getClipLeft();
        int clipBottom = RSRaster.getClipBottom();
        int clipRight = RSRaster.getClipRight();
        int clipTop = RSRaster.getClipTop();

        RSRaster.setBounds(y + widget.height, x, x + widget.width, y);
        int children = widget.children.length;

        for (int childIndex = 0; childIndex < children; childIndex++) {
            int currentX = widget.childX[childIndex] + x;
            int currentY = widget.childY[childIndex] + y - scroll;

            RSWidget child = RSWidget.lookup(widget.children[childIndex]);

            if (child == null) {
                continue;
            }

            currentX += child.horizontalDrawOffset;
            currentY += child.verticalDrawOffset;

            if (child.contentType > 0) {
                //method75(child);
            }

            if (child.group == RSWidget.TYPE_CONTAINER) {
                if (child.scrollPosition > child.scrollLimit - child.height) {
                    child.scrollPosition = child.scrollLimit - child.height;
                }
                if (child.scrollPosition < 0) {
                    child.scrollPosition = 0;
                }

                renderWidget(child, currentX, currentY, child.scrollPosition);
                if (child.scrollLimit > child.height) {
//                    drawScrollbar(child.height, child.scrollPosition, currentY,
//                            currentX + child.width, child.scrollLimit);
                }
            } else if (child.group != RSWidget.TYPE_MODEL_LIST) {
                if (child.group == RSWidget.TYPE_INVENTORY) {

                } else if (child.group == RSWidget.TYPE_RECTANGLE) {
                    int colour = child.defaultColour;

                    if (child.alpha == 0) {
                        if (child.filled) {
                            RSRaster.fillRectangle(currentX, currentY, child.width,
                                    child.height, colour);
                        } else {
                            RSRaster.drawRectangle(currentX, currentY, child.width,
                                    child.height, colour);
                        }
                    } else if (child.filled) {
                        RSRaster.fillRectangle(currentX, currentY, child.width, child.height,
                                colour, 256 - (child.alpha & 0xff));
                    } else {
                        RSRaster.drawRectangle(currentX, currentY, child.width, child.height,
                                colour, 256 - (child.alpha & 0xff));
                    }
                } else if (child.group == RSWidget.TYPE_TEXT) {
                    RSFont font = child.font;
                    String text = child.defaultText;

                    int colour;

                    colour = child.defaultColour;

                    if (child.optionType == RSWidget.OPTION_CONTINUE) {
                        text = "Please wait...";
                        colour = child.defaultColour;
                    }

                    if (RSRaster.width == 479) {
                        if (colour == 0xffff00) {
                            colour = 255;
                        } else if (colour == 49152) {
                            colour = 0xffffff;
                        }
                    }

                    for (int drawY = currentY + font.getVerticalSpace(); text
                            .length() > 0; drawY += font.getVerticalSpace()) {

                        int line = text.indexOf("\\n");
                        String drawn;
                        if (line != -1) {
                            drawn = text.substring(0, line);
                            text = text.substring(line + 2);
                        } else {
                            drawn = text;
                            text = "";
                        }

                        if (child.centeredText) {
                            font.shadowCentre(currentX + child.width / 2, drawY, drawn,
                                    child.shadowedText, colour);
                        } else {
                            font.shadow(currentX, drawY, drawn, child.shadowedText, colour);
                        }
                    }
                } else if (child.group == RSWidget.TYPE_SPRITE) {
                    RSSprite sprite = child.defaultSprite;

                    if (sprite != null) {
                        sprite.drawSprite(currentX, currentY);
                    }
                } else if (child.group == RSWidget.TYPE_MODEL) {

                } else if (child.group == RSWidget.TYPE_ITEM_LIST) {

                }
            }
        }

        RSRaster.setBounds(clipTop, clipLeft, clipRight, clipBottom);
    }

    public static void renderRectangle(RSWidget child, int currentX, int currentY) {
        if (child.group == RSWidget.TYPE_RECTANGLE) {
            int colour = child.defaultColour;

            if (child.alpha == 0) {
                if (child.filled) {
                    RSRaster.fillRectangle(currentX, currentY, child.width,
                            child.height, colour);
                } else {
                    RSRaster.drawRectangle(currentX, currentY, child.width,
                            child.height, colour);
                }
            } else if (child.filled) {
                RSRaster.fillRectangle(currentX, currentY, child.width, child.height,
                        colour, 256 - (child.alpha & 0xff));
            } else {
                RSRaster.drawRectangle(currentX, currentY, child.width, child.height,
                        colour, 256 - (child.alpha & 0xff));
            }
        }
    }

    public static void renderText(RSWidget child, int x, int y) {
        if (child.group == RSWidget.TYPE_TEXT) {

            RSFont font = child.font;
            String text = child.defaultText;

            int colour;

            colour = child.defaultColour;

            if (child.optionType == RSWidget.OPTION_CONTINUE) {
                text = "Please wait...";
                colour = child.defaultColour;
            }

            if (RSRaster.width == 479) {
                if (colour == 0xffff00) {
                    colour = 255;
                } else if (colour == 49152) {
                    colour = 0xffffff;
                }
            }

            for (int drawY = y + font.getVerticalSpace(); text
                    .length() > 0; drawY += font.getVerticalSpace()) {

                int line = text.indexOf("\\n");
                String drawn;
                if (line != -1) {
                    drawn = text.substring(0, line);
                    text = text.substring(line + 2);
                } else {
                    drawn = text;
                    text = "";
                }

                if (child.centeredText) {
                    font.shadowCentre(x + child.width / 2, drawY, drawn,
                            child.shadowedText, colour);
                } else {
                    font.shadow(x, drawY, drawn, child.shadowedText, colour);
                }
            }
        }
    }

}
