package eu.thog92.launcher.version;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTypeAdapter
  implements JsonSerializer<Date>, JsonDeserializer<Date>
{
  private final DateFormat enUsFormat;
  private final DateFormat iso8601Format;
  
  public DateTypeAdapter()
  {
    this.enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
    this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
  }
  
  @Override
public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException
  {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }
    Date date = deserializeToDate(json.getAsString());
    if (typeOfT == Date.class) {
      return date;
    }
    throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
  }
  
  @Override
public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context)
  {
    synchronized (this.enUsFormat)
    {
      return new JsonPrimitive(serializeToString(src));
    }
  }
  
  public Date deserializeToDate(String string)
  {
    synchronized (this.enUsFormat)
    {
      try
      {
        return this.enUsFormat.parse(string);
      }
      catch (ParseException ignored)
      {
        try
        {
          return this.iso8601Format.parse(string);
        }
        catch (ParseException ignored1)
        {
          try
          {
            String cleaned = string.replace("Z", "+00:00");
            cleaned = cleaned.substring(0, 22) + cleaned.substring(23);
            return this.iso8601Format.parse(cleaned);
          }
          catch (Exception e)
          {
            throw new JsonSyntaxException("Invalid date: " + string, e);
          }
        }
      }
    }
  }
  
  public String serializeToString(Date date)
  {
    synchronized (this.enUsFormat)
    {
      String result = this.iso8601Format.format(date);
      return result.substring(0, 22) + ":" + result.substring(22);
    }
  }
}
