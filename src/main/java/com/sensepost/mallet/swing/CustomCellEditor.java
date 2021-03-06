package com.sensepost.mallet.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.view.mxICellEditor;
import com.mxgraph.view.mxGraph;

public class CustomCellEditor implements mxICellEditor {

	private mxGraphComponent graphComponent;

	private Object editingCell = null;

	private GraphNodeEditor editor = new GraphNodeEditor();
	private JDialog dialog;

	public CustomCellEditor(mxGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
		dialog = new JDialog((JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, graphComponent));
		dialog.setModal(true);
		dialog.getRootPane().setLayout(new BorderLayout());
		dialog.getRootPane().add(editor, BorderLayout.CENTER);
		Action cancelAction = new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				stopEditing(true);
			}

		};
		Action okAction = new AbstractAction("Ok") {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				stopEditing(false);
			}

		};
		editor.setActions(cancelAction, okAction);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				dialog.setVisible(false);
				stopEditing(true);
			}

		});
		dialog.pack();
	}

	@Override
	public Object getEditingCell() {
		return editingCell;
	}

	@Override
	public void startEditing(Object cell, EventObject trigger) {
		if (editingCell != null) {
			stopEditing(true);
		}

		editingCell = cell;
		Object value = graphComponent.getGraph().getModel().getValue(cell);
		if (value instanceof Element) {
			editor.setGraphNode((Element) value);
			dialog.pack();
			dialog.setLocationRelativeTo(graphComponent);
			dialog.setVisible(true);
		}
	}

	@Override
	public void stopEditing(boolean cancel) {
		if (editingCell == null)
			return;

		if (!cancel)
			graphComponent.getGraph().getModel().setValue(editingCell, editor.getGraphNode());
		editingCell = null;
		editor.setGraphNode(null);
		layoutGraph(graphComponent.getGraph());
	}

	private void layoutGraph(mxGraph graph) {
		mxIGraphLayout layout = new mxHierarchicalLayout(graph);
		graph.getModel().beginUpdate();
		try {
			Object[] cells = graph.getChildCells(graph.getDefaultParent());
			for (int i = 0; i < cells.length; i++) {
				graph.updateCellSize(cells[i]);
			}

			layout.execute(graph.getDefaultParent());
		} finally {
			graph.getModel().endUpdate();
		}
	}

}
