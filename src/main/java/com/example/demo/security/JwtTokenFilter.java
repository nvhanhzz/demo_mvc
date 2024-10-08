package com.example.demo.security;

import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException {
        try {
            // Lấy JWT token từ cookie
            String token = getTokenFromCookies(request);

            // Nếu người dùng đã có token và đang cố truy cập vào endpoint "/auth", chuyển hướng đến "/user"
            if (token != null && isBypassToken(request)) {
                String username = jwtTokenUtil.extractUsername(token);
                if (username != null && jwtTokenUtil.validateToken(token, userDetailsService.loadUserByUsername(username))) {
                    // Nếu token hợp lệ và đang vào /auth, chuyển hướng người dùng đến trang /user
                    response.sendRedirect("/user");
                    return;
                }
            }

            // Bỏ qua kiểm tra xác thực JWT cho những endpoint không yêu cầu
            if (isBypassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Nếu không có token
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Trích xuất thông tin username từ token
            String username = jwtTokenUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    // Nếu token hợp lệ, thiết lập Authentication cho request hiện tại
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    // Token không hợp lệ, xóa cookie
                    clearJwtCookie(response);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException e) {
            // Token hết hạn hoặc không hợp lệ, xóa cookie
            clearJwtCookie(response);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            clearJwtCookie(response);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized request");
        }
    }

    /**
     * Lấy JWT token từ cookie của request.
     */
    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Xóa JWT token khỏi cookie bằng cách đặt cookie mới với giá trị rỗng và maxAge bằng 0.
     */
    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", null); // Đặt giá trị cookie thành null
        cookie.setPath("/"); // Đảm bảo đường dẫn chính xác
        cookie.setMaxAge(0); // Đặt thời gian sống của cookie thành 0 để xóa nó
        cookie.setHttpOnly(true); // Đảm bảo cookie chỉ được truy cập thông qua HTTP (không phải JavaScript)
        response.addCookie(cookie); // Thêm cookie vào phản hồi
    }

    /**
     * Kiểm tra xem request có cần bypass kiểm tra JWT không.
     * Bỏ qua cho tất cả các endpoint bắt đầu bằng "/auth" và "/favicon.ico".
     */
    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        String requestPath = request.getServletPath();
        return requestPath.startsWith("/auth") || requestPath.equals("/favicon.ico");
    }
}
