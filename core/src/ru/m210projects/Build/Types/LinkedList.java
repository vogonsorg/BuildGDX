package ru.m210projects.Build.Types;

public class LinkedList<T> {
	
	public abstract static class Node<T> {
		protected Node<T> next;
		protected Node<T> prev;

		protected final Node<T> unlink() {
			Node<T> t = next;
			if (prev != null)
				prev.next = next;
			if (next != null)
				next.prev = prev;
			next = prev = null;
			return t;
		}

		protected final void insertAfter(Node<T> node) {
			node.next = next;
			node.prev = this;
			if (next != null)
				next.prev = node;
			next = node;
		}

		protected final void insertBefore(Node<T> node) {
			node.next = this;
			node.prev = prev;
			if (prev != null)
				prev.next = node;
			prev = node;
		}
		
		public Node<T> next() {
			return next;
		}
		
		public Node<T> prev() {
			return prev;
		}
		
		public abstract T getValue();
	}

	protected Node<T> head, tail;
	protected int size;

	public void clear() {
		Node<T> node = head;
		while(node != null) {
			Node<T> next = node.next;
			node.prev = null;
			node.next = null;
			node = next;
		}

		head = tail = null;
		size = 0;
	}
	
	public int size() {
		return size;
	}

	public boolean remove(Node<T> item) {
		int s = size;
		if (s == 0)
			return false;
		
		Node<T> node = head;
		while (node != null) {
			if (node == item) {
				node = node.unlink();
				if(item == head)
					head = node;
				if(item == tail)
					tail = node;
				
				if(head != null && head == head.next)
					System.err.println("aaa0");
				
				if (head != null && head.prev != null) {
					
					System.err.println("ah11");
				}
				
				size--;
				return true;
			} else node = node.next;
		}
		return false;
	}

	public void insert(Node<T> node) {
		if(node.prev != null)
			System.err.println("sss");
		
		
		if (tail != null) {
			tail.next = node;
			node.prev = tail;
		} else 
			head = node;
		tail = node;
		size++;
	}
	
	public void insertBefore(Node<T> node, Node<T> item) {
		if(node == null) {
			insert(item);
			return;
		}
		
		if (head != null && head.prev != null) {
			head.prev = null;
		}
		
		node.insertBefore(item);
		if (node == head) 
			head = item;
		size++;
	}
	
	public T next(Node<T> node) {
		if(node.next != null)
			return node.next.getValue();
		
		return null;
	}

	public T getFirst() {
		if(head != null)
			return head.getValue();
		
		return null;
	}

	public T getLast() {
		if(tail != null)
			return tail.getValue();
		
		return null;
	}
}
