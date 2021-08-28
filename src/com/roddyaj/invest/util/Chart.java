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

	public List<Point> getPoints()
	{
		return points;
	}

	public List<HLine> getHLines()
	{
		return hLines;
	}

	public List<Rect> getRectangles()
	{
		return rectangles;
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
		StringBuilder sb = new StringBuilder();
		sb.append("<svg height=\"").append(svgHeight).append("\" width=\"").append(svgWidth).append("\">");
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
			StringBuilder sb = new StringBuilder();
			double cx = chart.getX(x, svgWidth);
			double cy = chart.getY(y, svgHeight);
			double r = 2.;
			sb.append("<circle cx=\"").append(cx).append("\" cy=\"").append(cy).append("\" r=\"").append(r).append("\" fill=\"").append(color)
					.append("\" />");
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
			StringBuilder sb = new StringBuilder();
			double yChart = chart.getY(y, svgHeight);
			double x1 = 0;
			double y1 = yChart;
			double x2 = svgWidth;
			double y2 = yChart;
			sb.append("<line x1=\"").append(x1).append("\" y1=\"").append(y1).append("\" x2=\"").append(x2).append("\" y2=\"").append(y2)
					.append("\" stroke=\"").append(color).append("\" />");
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

		public Rect(double x, double y, double width, double height, String color)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.color = color;
		}

		public String toSvg(Chart chart, int svgHeight, int svgWidth)
		{
			StringBuilder sb = new StringBuilder();
			double rectX = chart.getX(x, svgWidth);
			double rectY = chart.getY(y, svgHeight);
			double rectWidth = chart.getX(width, svgWidth) - chart.getX(0, svgWidth);
			double rectHeight = chart.getY(0, svgHeight) - chart.getY(height, svgHeight);
			sb.append("<rect x=\"").append(rectX).append("\" y=\"").append(rectY).append("\" width=\"").append(rectWidth).append("\" height=\"")
					.append(rectHeight).append("\" fill=\"").append(color).append("\" />");
			return sb.toString();
		}
	}
}
