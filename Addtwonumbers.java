// Step 1: LeetCode problem 2 requires adding two numbers stored as linked
// lists.
// Step 2: Remember that each list stores digits from the ones place to the
// highest place.
//
// Step 3: Define the required method signature:
// public ListNode addTwoNumbers(ListNode l1, ListNode l2)
//
// Step 4: Create a dummy head node for the result.
// Syntax template: ListNode dummy = new ListNode(0);
// Hint: dummy simplifies returning the first real result node.
//
// Step 5: Create a pointer for the current position in the result list.
// Syntax template: ListNode current = dummy;
//
// Step 6: Initialize a carry variable to zero.
// Syntax template: int carry = 0;
//
// Step 7: Continue a loop while l1 is not null, l2 is not null, or carry is not
// zero.
// Syntax template: while (l1 != null || l2 != null || carry != 0) {
// Hint: use a while loop because the lists may have different lengths.
//
// Step 8: Get l1's digit, or use 0 if l1 has ended.
// Syntax template: int value1 = (l1 != null) ? l1.val : 0;
//
// Step 9: Get l2's digit, or use 0 if l2 has ended.
// Syntax template: int value2 = (l2 != null) ? l2.val : 0;
//
// Step 10: Add the two digits and the carry.
// Syntax template: int sum = value1 + value2 + carry;
//
// Step 11: Update carry to the tens place of sum.
// Hint: integer division by 10 gives the carry.
// Syntax template: carry = sum / 10;
//
// Step 12: Create a result node containing the ones place of sum.
// Hint: the remainder after division by 10 gives the digit.
// Syntax template: current.next = new ListNode(sum % 10);
//
// Step 13: Move the result pointer to the newly created node.
// Syntax template: current = current.next;
//
// Step 14: Move l1 to its next node only if it has one.
// Syntax template:
// if (l1 != null) {
// l1 = l1.next;
// }
//
// Step 15: Move l2 to its next node only if it has one.
// Syntax template:
// if (l2 != null) {
// l2 = l2.next;
// }
//
// Step 16: Close the while loop.
// Syntax template: }
//
// Step 17: Return the first real node after the dummy head.
// Syntax template: return dummy.next;
//
// Step 18: After implementation, test unequal lengths, a remaining carry, and
// zero values.
// Step 19: Confirm the time complexity is O(max(n, m)) and extra list space is
// O(max(n, m)).

public class Addtwonumbers {
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
            int val1 = (l1 != null) ? l1.val : 0;
            int val2 = (l2 != null) ? l2.val : 0;
            int sum = val1 + val2 + carry;
            carry = sum / 10;

            current.next = new ListNode(sum % 10);
            current = current.next;

            if (l1 != null) {
                l1 = l1.next;
            }
            if (l2 != null) {
                l2 = l2.next;
            }
        }
        return dummy.next;
    }

    // Step 20: Write the main method to test addTwoNumbers.
    // Required signature:
    // public static void main(String[] args) {
    // Hint: main must be static and receives command-line arguments as a String
    // array.
    //
    // Step 21: Create the linked-list nodes for the first number.
    // Example syntax template:
    // ListNode l1 = new ListNode(2);
    // l1.next = new ListNode(4);
    // l1.next.next = new ListNode(3);
    // Hint: The digits must be stored in reverse order, from ones to highest place.
    //
    // Step 22: Create the linked-list nodes for the second number.
    // Example syntax template:
    // ListNode l2 = new ListNode(5);
    // l2.next = new ListNode(6);
    // l2.next.next = new ListNode(4);
    //
    // Step 23: Call addTwoNumbers and store the resulting linked list.
    // Syntax template:
    // ListNode result = addTwoNumbers(l1, l2);
    //
    // Step 24: Traverse the result list and print each digit.
    // Hint: Use a current pointer and a while loop to print values until the end.
    // Syntax template:
    // while (result != null) {
    // System.out.print(result.val);
    // result = result.next;
    // }
    //
    // Step 25: Add a newline after printing the result.
    // Syntax template:
    // System.out.println();
    //
    // Step 26: Test unequal-length lists and a remaining carry.
    // Hint: Add separate test cases by creating new ListNode chains.
    //
    // Step 27: Test zero values, such as adding [0] and [0].
    // Hint: Also verify the special case where a carry creates one final node.
    //
    // Step 28: Close the main method.
    // Syntax template:
    // }

    public static void main(String[] args) {
        ListNode l1 = new ListNode(2);
        l1.next = new ListNode(4);
        l1.next.next = new ListNode(3);

        ListNode l2 = new ListNode(5);
        l2.next = new ListNode(6);
        l2.next.next = new ListNode(4);

        ListNode result = addTwoNumbers(l1, l2);
        while (result != null) {
            System.out.print(result.val);
            result = result.next;
        }
        System.out.println();
    }
}