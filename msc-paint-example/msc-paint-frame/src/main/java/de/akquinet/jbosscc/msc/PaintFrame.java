package de.akquinet.jbosscc.msc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * This class represents the main application class, which is a JFrame subclass
 * that manages a toolbar of shapes and a drawing canvas. This class does not
 * directly interact with the underlying OSGi framework; instead, it is injected
 * with the available <tt>SimpleShape</tt> instances to eliminate any
 * dependencies on the OSGi application programming interfaces.
 **/
public class PaintFrame extends JFrame implements MouseListener,
		MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static final int BOX = 54;
	private JToolBar m_toolbar;
	private String m_selected;
	private JPanel m_panel;
	private ShapeComponent m_selectedComponent;
	private Map<String, SimpleShape> m_shapes = new HashMap<String, SimpleShape>();
	private ActionListener m_reusableActionListener = new ShapeActionListener();
	private DefaultShape defaultShape = new DefaultShape();

	/**
	 * Default constructor that populates the main window.
	 **/
	public PaintFrame() {
		super("PaintFrame");

		m_toolbar = new JToolBar("Toolbar");
		m_panel = new JPanel();
		m_panel.setBackground(Color.WHITE);
		m_panel.setLayout(null);
		m_panel.setMinimumSize(new Dimension(400, 400));
		m_panel.addMouseListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(m_toolbar, BorderLayout.NORTH);
		getContentPane().add(m_panel, BorderLayout.CENTER);
		setSize(400, 400);
	}

	/**
	 * This method sets the currently selected shape to be used for drawing on
	 * the canvas.
	 *
	 * @param name
	 *            The name of the shape to use for drawing on the canvas.
	 **/
	public void selectShape(String name) {
		m_selected = name;
	}

	/**
	 * Retrieves the available <tt>SimpleShape</tt> associated with the given
	 * name.
	 *
	 * @param name
	 *            The name of the <tt>SimpleShape</tt> to retrieve.
	 * @return The corresponding <tt>SimpleShape</tt> instance if available or
	 *         <tt>null</tt>.
	 **/
	public SimpleShape getShape(String name) {
		SimpleShape simpleShape = m_shapes.get(name);
		return simpleShape != null ? simpleShape : defaultShape;
	}

	/**
	 * Injects an available <tt>SimpleShape</tt> into the drawing frame.
	 *
	 * @param name
	 *            The name of the injected <tt>SimpleShape</tt>.
	 * @param icon
	 *            The icon associated with the injected <tt>SimpleShape</tt>.
	 * @param shape
	 *            The injected <tt>SimpleShape</tt> instance.
	 **/
	public void addShape(SimpleShape shape) {
		m_shapes.put(shape.getName(), shape);
		JButton button = new JButton(shape.getIcon());
		button.setActionCommand(shape.getName());
		button.setToolTipText(shape.getName());
		button.addActionListener(m_reusableActionListener);

		if (m_selected == null) {
			button.doClick();
		}

		m_toolbar.add(button);
		m_toolbar.validate();
		repaint();
	}

	/**
	 * Removes a no longer available <tt>SimpleShape</tt> from the drawing
	 * frame.
	 *
	 * @param name
	 *            The name of the <tt>SimpleShape</tt> to remove.
	 **/
	public void removeShape(String name) {
		m_shapes.remove(name);

		if ((m_selected != null) && m_selected.equals(name)) {
			m_selected = null;
		}

		for (int i = 0; i < m_toolbar.getComponentCount(); i++) {
			JButton sb = (JButton) m_toolbar.getComponent(i);
			if (sb.getActionCommand().equals(name)) {
				m_toolbar.remove(i);
				m_toolbar.invalidate();
				validate();
				repaint();
				break;
			}
		}

		if ((m_selected == null) && (m_toolbar.getComponentCount() > 0)) {
			((JButton) m_toolbar.getComponent(0)).doClick();
		}
	}

	/**
	 * Implements method for the <tt>MouseListener</tt> interface to draw the
	 * selected shape into the drawing canvas.
	 *
	 * @param evt
	 *            The associated mouse event.
	 **/
	public void mouseClicked(MouseEvent evt) {
		if (m_selected == null) {
			return;
		}

		if (m_panel.contains(evt.getX(), evt.getY())) {
			ShapeComponent sc = new ShapeComponent(this, m_selected);
			sc.setBounds(evt.getX() - BOX / 2, evt.getY() - BOX / 2, BOX, BOX);
			m_panel.add(sc, 0);
			m_panel.validate();
			m_panel.repaint(sc.getBounds());
		}
	}

	/**
	 * Implements an empty method for the <tt>MouseListener</tt> interface.
	 *
	 * @param evt
	 *            The associated mouse event.
	 **/
	public void mouseEntered(MouseEvent evt) {
	}

	/**
	 * Implements an empty method for the <tt>MouseListener</tt> interface.
	 *
	 * @param evt
	 *            The associated mouse event.
	 **/
	public void mouseExited(MouseEvent evt) {
	}

	/**
	 * Implements method for the <tt>MouseListener</tt> interface to initiate
	 * shape dragging.
	 *
	 * @param evt
	 *            The associated mouse event.
	 **/
	public void mousePressed(MouseEvent evt) {
		Component c = m_panel.getComponentAt(evt.getPoint());
		if (c instanceof ShapeComponent) {
			m_selectedComponent = (ShapeComponent) c;
			m_panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			m_panel.addMouseMotionListener(this);
			m_selectedComponent.repaint();
		}
	}

	/**
	 * Implements method for the <tt>MouseListener</tt> interface to complete
	 * shape dragging.
	 *
	 * @param evt
	 *            The associated mouse event.
	 **/
	public void mouseReleased(MouseEvent evt) {
		if (m_selectedComponent != null) {
			m_panel.removeMouseMotionListener(this);
			m_panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			m_selectedComponent.setBounds(evt.getX() - BOX / 2, evt.getY()
					- BOX / 2, BOX, BOX);
			m_selectedComponent.repaint();
			m_selectedComponent = null;
		}
	}

	/**
	 * Implements method for the <tt>MouseMotionListener</tt> interface to move
	 * a dragged shape.
	 *
	 * @param evt
	 *            The associated mouse event.
	 **/
	public void mouseDragged(MouseEvent evt) {
		m_selectedComponent.setBounds(evt.getX() - BOX / 2, evt.getY() - BOX
				/ 2, BOX, BOX);
	}

	/**
	 * Implements an empty method for the <tt>MouseMotionListener</tt>
	 * interface.
	 *
	 * @param evt
	 *            The associated mouse event.
	 **/
	public void mouseMoved(MouseEvent evt) {
	}

	/**
	 * Simple action listener for shape tool bar buttons that sets the drawing
	 * frame's currently selected shape when receiving an action event.
	 **/
	private class ShapeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			selectShape(evt.getActionCommand());
		}
	}


}
