import java.util.*;

public class Main {
	static class ListNode {
		int val;
		ListNode next;

		ListNode(int val) {
			this.val = val;
		}
	}

	public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
		ListNode dummy = new ListNode(0);
		ListNode current = dummy;
		int carry = 0;

		while (l1 != null || l2 != null || carry != 0) {
			int sum = carry;

			if (l1 != null) {
				sum += l1.val;
				l1 = l1.next;
			}
			if (l2 != null) {
				sum += l2.val;
				l2 = l2.next;
			}

			carry = sum / 10;
			current.next = new ListNode(sum % 10); // new creates a node
			current = current.next;
		}

		return dummy.next;
	}

	public static void main(String[] args) {
		// Connect nodes using next to create each linked list.
		ListNode l1 = new ListNode(2);
		l1.next = new ListNode(4);
		l1.next.next = new ListNode(3);

		ListNode l2 = new ListNode(5);
		l2.next = new ListNode(6);
		l2.next.next = new ListNode(4);

		ListNode result = addTwoNumbers(l1, l2);

		// Follow next until it is null to visit all nodes.
		for (ListNode node = result; node != null; node = node.next) {
			System.out.print(node.val);
			if (node.next != null) {
				System.out.print(" -> ");
			}
		}
	}
}