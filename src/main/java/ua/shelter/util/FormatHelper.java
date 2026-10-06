final class FormatHelper {
         private FormatHelper() {
         }

         public static String normalize(String value, String message) {
                  ValidationHelper.requireNotBlank(value, message);
                  return value.trim().toUpperCase();
         }

         public static String capitalize(String value, String message) {
                  ValidationHelper.requireNotBlank(value, message);
                  value = value.trim();
                  int n = value.length();
                  char[] temp = new char[n];
                  temp = value.toCharArray();
                  temp[0]=Character.toUpperCase(temp[0]);
                  if (n > 1) {
                           for (int i = 1; i < n; i++) {
                                    temp[i] = Character.toLowerCase(temp[i]);
                           }
                  }
                  return new String(temp);
                  
                  
         }

         public static String formatMoney(double value, String message) {
                  ValidationHelper.requirePositive(value, message);
                  return String.format("%.2f грн", value);
         }
}