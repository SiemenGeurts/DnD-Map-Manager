package helpers.codecs.versions;

import data.mapdata.Map;

public class DecoderV2 extends DecoderV1 {
	@Override
	public Map decodeMap(String string) {
		int index = string.lastIndexOf(';');
		Map map = super.decodeMap(string.substring(0,index));
		byte[][] mask = new byte[map.getHeight()][map.getWidth()];
		char[] maskStr = string.substring(index+1).toCharArray();
		if(maskStr.length!=mask.length*mask[0].length)
			throw new RuntimeException("Mask size does not fit map size.");
		int k = 0;
		for(int i = 0; i < mask.length; i++) 
			for(int j = 0; j < mask[0].length; j++, k++)
				mask[i][j] = (byte)(maskStr[k]-62);
		map.setMask(mask);
		return map;
	}
}