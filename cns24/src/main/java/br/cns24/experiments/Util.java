/**
 * 
 */
package br.cns24.experiments;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.NumberFormat;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * @author Danilo
 * 
 */
public class Util {
	public static final NumberFormat decimalFormat = NumberFormat.getInstance();
	public static final NumberFormat simpleFormat = NumberFormat.getInstance();

	static {
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setMaximumFractionDigits(2);
		simpleFormat.setMinimumFractionDigits(0);
		simpleFormat.setMaximumFractionDigits(0);
	}

	/**
	 * Retorna uma imagem, considerando a possibilidade de estar empacotado em
	 * um arquivo JAR ou n�o.
	 * 
	 * @param imageName
	 *            Nome da imagem
	 * @return Objeto representando a imagem
	 */
	public static Image loadImage(final String imageName) {
		final ClassLoader loader = Util.class.getClassLoader();
		Image image = null;
		InputStream is = (InputStream) AccessController
				.doPrivileged(new PrivilegedAction<InputStream>() {
					public InputStream run() {
						if (loader != null) {
							return loader.getResourceAsStream(imageName);
						} else {
							return ClassLoader
									.getSystemResourceAsStream(imageName);
						}
					}
				});
		if (is != null) {
			try {
				final int BlockLen = 256;
				int offset = 0;
				int len;
				byte imageData[] = new byte[BlockLen];
				while ((len = is.read(imageData, offset, imageData.length
						- offset)) > 0) {
					if (len == (imageData.length - offset)) {
						byte newData[] = new byte[imageData.length * 2];
						System.arraycopy(imageData, 0, newData, 0,
								imageData.length);
						imageData = newData;
						newData = null;
					}
					offset += len;
				}
				image = java.awt.Toolkit.getDefaultToolkit().createImage(
						imageData);
			} catch (java.io.IOException ex) {
			}
		}

		if (image == null) {
			image = new ImageIcon(imageName).getImage();
		}
		return image;
	}

	/**
	 * Retorna um input stream, considerando a possibilidade de estar empacotado
	 * em um arquivo JAR ou n�o.
	 * 
	 * @param fileName
	 *            Nome do arquivo
	 * @return Objeto InputStream
	 */
	public static InputStream getInputStream(final String fileName)
			throws FileNotFoundException {
		final ClassLoader loader = Util.class.getClassLoader();
		InputStream is = (InputStream) AccessController
				.doPrivileged(new PrivilegedAction<InputStream>() {
					public InputStream run() {
						if (loader != null) {
							return loader.getResourceAsStream(fileName);
						} else {
							return ClassLoader
									.getSystemResourceAsStream(fileName);
						}
					}
				});
		if (is == null) {
			is = new FileInputStream(new File(fileName));
		}
		return is;
	}

	public static void writeImageToJPG(File file, BufferedImage bufferedImage)
			throws IOException {
		ImageIO.write(bufferedImage, "jpg", file);
	}

	public static void writeImageToBmp(File file, BufferedImage bufferedImage)
			throws IOException {
		ImageIO.write(bufferedImage, "bmp", file);
	}

	public static void doZip(String filename, String zipfilename) {
		try {
			byte[] buf = new byte[1024];
			FileInputStream fis = new FileInputStream(filename);
			fis.read(buf, 0, buf.length);

			CRC32 crc = new CRC32();
			ZipOutputStream s = new ZipOutputStream(
					(OutputStream) new FileOutputStream(zipfilename));

			s.setLevel(6);

			ZipEntry entry = new ZipEntry(filename);
			entry.setSize((long) buf.length);
			crc.reset();
			crc.update(buf);
			entry.setCrc(crc.getValue());
			s.putNextEntry(entry);
			s.write(buf, 0, buf.length);
			s.finish();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transferindo bytes de entrada para sa�da
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
}
