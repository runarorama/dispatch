package net.databinder.components.hibernate.datatree.controllinks;

import javax.swing.tree.DefaultMutableTreeNode;

import net.databinder.DataStaticService;
import net.databinder.components.hibernate.datatree.DataTree;
import net.databinder.components.hibernate.datatree.IDataTreeNode;
import net.databinder.components.hibernate.datatree.SingleSelectionDataTree;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;


/**
 * Delete the selected node. Works only with {@link SingleSelectionDataTree} to
 * avoid dealing with multiple selected nodes.
 * <p>
 * The root cannot be deleted, it must be handled elsewhere in the application.
 * This follows the Sun <a
 * href="http://java.sun.com/docs/books/tutorial/uiswing/components/tree.html">How
 * to Use Trees</a> tutorial, example DynamicTreeDemo.
 * </p>
 * 
 * @author Thomas Kappler
 * 
 * @param <T>
 *            see {@link DataTree}
 */
public class DataTreeDeleteButton<T extends IDataTreeNode<T>> extends AjaxButton {

	private SingleSelectionDataTree<T> tree;
	private boolean deleteOnlyLeafs = true;
	
	public DataTreeDeleteButton(String id, SingleSelectionDataTree<T> tree) {
		super(id);
		this.tree = tree;
		setDefaultFormProcessing(false);
	}

	public DataTreeDeleteButton(String id, SingleSelectionDataTree<T> tree,
			boolean deleteOnlyLeafs) {
		this(id, tree);
		this.deleteOnlyLeafs = deleteOnlyLeafs;
	}

	@Override
	public boolean isEnabled() {
		DefaultMutableTreeNode selected = tree.getSelectedTreeNode(); 
		if (selected == null) {
			return false;
		}
		if (selected.isRoot()) {
			return false;
		}
		if (deleteOnlyLeafs) {
			return selected.isLeaf();
		}
		
		return true;
	}

	@Override
	protected void onSubmit(AjaxRequestTarget target, Form form) {
		DefaultMutableTreeNode selectedNode = tree.getSelectedTreeNode();
		T selected = tree.getSelectedUserObject();
	
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) 
				selectedNode.getParent();
		T parent = tree.getObjectFromNode(parentNode);

		parentNode.remove(selectedNode);
		parent.removeChild(selected);
		DataStaticService.getHibernateSession().delete(selected);
		
		tree.getTreeState().selectNode(parentNode, true);
		tree.repaint(target);
		tree.updateDependentComponents(target, parentNode);
		
		DataStaticService.getHibernateSession().getTransaction().commit();
	}
}