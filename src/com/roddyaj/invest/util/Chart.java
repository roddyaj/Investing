package com.roddyaj.invest.util;

import java.util.ArrayList;
import java.util.List;

public class Chart
{
	private final double xMin;
	private final double xMax;
	private final double yMin;
	private final double yMax;

	private final List<Point> points = new ArrayList<>();

	private final List<HLine> hLines = new ArrayList<>();

	private final List<Rect> rectangles = new ArrayList<>();

	public Chart(double xMin, double xMax, double yMin, double yMax)
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
	}

	public void addPoint(Point point)
	{
		points.add(point);
	}

	public void addHLine(HLine hLine)
	{
		hLines.add(hLine);
	}

	public void addRectangle(Rect rect)
	{
		rectangles.add(rect);
	}

	public void addRectangle(double yMin, double yMax, String color)
	{
		rectangles.add(new Rect(xMin, xMax, yMin, yMax, color));
	}

	public double getX(double x, int width)
	{
		final int BORDER_WIDTH = 0;
		double xNorm = (x - xMin) / (xMax - xMin);
		return xNorm * (width - BORDER_WIDTH * 2) + BORDER_WIDTH;
	}

	public double getY(double y, int height)
	{
		final int BORDER_WIDTH = 0;
		double yNorm = 1. - (y - yMin) / (yMax - yMin);
		return yNorm * (height - BORDER_WIDTH * 2) + BORDER_WIDTH;
	}

	public String toSvg(int svgHeight, int svgWidth)
	{
		StringBuilder sb = new StringBuilder("<svg");
		appendKeyValue(sb, "height", svgHeight);
		appendKeyValue(sb, "width", svgWidth);
		sb.append(">");
		sb.append("<rect width=\"100%\" height=\"100%\" fill=\"white\" />");
		for (Rect rect : rectangles)
			sb.append(rect.toSvg(this, svgHeight, svgWidth));
		for (HLine line : hLines)
			sb.append(line.toSvg(this, svgHeight, svgWidth));
		for (Point point : points)
			sb.append(point.toSvg(this, svgHeight, svgWidth));
		sb.append("</svg>");
		return sb.toString();
	}

	private static void appendKeyValue(StringBuilder sb, String key, double value)
	{
		sb.append(' ').append(key).append("=\"");
		if (value == (long)value)
			sb.append((long)value);
		else
			sb.append(String.format("%.2f", value));
		sb.append("\"");
	}

	private static void appendKeyValue(StringBuilder sb, String key, int value)
	{
		sb.append(' ').append(key).append("=\"").append(value).append("\"");
	}

	private static void appendKeyValue(StringBuilder sb, String key, String value)
	{
		sb.append(' ').append(key).append("=\"").append(value).append("\"");
	}

	public static class Point
	{
		private final double x;
		private final double y;
		private final String color;

		public Point(double x, double y, String color)
		{
			this.x = x;
			this.y = y;
			this.color = color;
		}

		public String toSvg(Chart chart, int svgHeight, int svgWidth)
		{
			StringBuilder sb = new StringBuilder("<circle");
			appendKeyValue(sb, "cx", chart.getX(x, svgWidth));
			appendKeyValue(sb, "cy", chart.getY(y, svgHeight));
			appendKeyValue(sb, "r", 2);
			appendKeyValue(sb, "fill", color);
			sb.append(" />");
			return sb.toString();
		}
	}

	public static class HLine
	{
		private final double y;
		private final String color;

		public HLine(double y, String color)
		{
			this.y = y;
			this.color = color;
		}

		public String toSvg(Chart chart, int svgHeight, int svgWidth)
		{
			double yChart = chart.getY(y, svgHeight);
			StringBuilder sb = new StringBuilder("<line");
			appendKeyValue(sb, "x1", 0);
			appendKeyValue(sb, "y1", yChart);
			appendKeyValue(sb, "x2", svgWidth);
			appendKeyValue(sb, "y2", yChart);
			appendKeyValue(sb, "stroke", color);
			sb.append(" />");
			return sb.toString();
		}
	}

	public static class Rect
	{
		private final double x;
		private final double y;
		private final double width;
		private final double height;
		private final String color;

		public Rect(double xMin, double xMax, double yMin, double yMax, String color)
		{
			this.x = xMin;
			this.y = yMax;
			this.width = xMax - xMin;
			this.height = yMax - yMin;
			this.color = color;
		}

		public String toSvg(Chart chart, int svgHeight, int svgWidth)
		{
			StringBuilder sb = new StringBuilder("<rect");
			appendKeyValue(sb, "x", chart.getX(x, svgWidth));
			appendKeyValue(sb, "y", chart.getY(y, svgHeight));
			appendKeyValue(sb, "width", chart.getX(width, svgWidth) - chart.getX(0, svgWidth));
			appendKeyValue(sb, "height", chart.getY(0, svgHeight) - chart.getY(height, svgHeight));
			appendKeyValue(sb, "fill", color);
			sb.append(" />");
			return sb.toString();
		}
	}
}
